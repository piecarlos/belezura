package com.salu.calendario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

object BackgroundManager {
    private const val TAG = "BackgroundManager"
    private const val CUSTOM_BG_FILENAME = "bg_custom.png"
    const val TARGET_WIDTH = 1080
    const val TARGET_HEIGHT = 1920

    @Volatile
    private var cachedBitmap: Bitmap? = null

    fun getBackgroundBitmap(context: Context): Bitmap {
        cachedBitmap?.let {
            if (!it.isRecycled) return it
        }

        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val customFile = File(context.filesDir, CUSTOM_BG_FILENAME)
        val loaded = if (customFile.exists() && customFile.length() > 0) {
            try {
                BitmapFactory.decodeFile(customFile.absolutePath, options)
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao carregar imagem personalizada do disco: ${e.message}")
                null
            }
        } else {
            null
        }

        val finalBitmap = loaded ?: run {
            try {
                BitmapFactory.decodeResource(context.resources, R.drawable.bg_base, options)
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao carregar R.drawable.bg_base: ${e.message}")
                null
            }
        } ?: createFallbackBitmap()

        cachedBitmap = finalBitmap
        return finalBitmap
    }

    fun hasCustomBackground(context: Context): Boolean {
        val customFile = File(context.filesDir, CUSTOM_BG_FILENAME)
        return customFile.exists() && customFile.length() > 0
    }

    fun setCustomBackground(context: Context, uri: Uri): Result<Unit> {
        return try {
            val srcBitmap = decodeSourceBitmap(context, uri)

            // Criar bitmap destino limpo com resolução exata de 1080 x 1920
            val targetBitmap = Bitmap.createBitmap(TARGET_WIDTH, TARGET_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(targetBitmap)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

            // Zoom central automático (center-crop) sem erro de borda
            val scale = max(
                TARGET_WIDTH.toFloat() / srcBitmap.width.toFloat(),
                TARGET_HEIGHT.toFloat() / srcBitmap.height.toFloat()
            )
            val dx = (TARGET_WIDTH - srcBitmap.width * scale) / 2f
            val dy = (TARGET_HEIGHT - srcBitmap.height * scale) / 2f

            val transformMatrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(dx, dy)
            }
            canvas.drawBitmap(srcBitmap, transformMatrix, paint)
            srcBitmap.recycle()

            // Salvar resultado sem perdas em filesDir/bg_custom.png
            val customFile = File(context.filesDir, CUSTOM_BG_FILENAME)
            FileOutputStream(customFile).use { out ->
                targetBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            targetBitmap.recycle()

            invalidateCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar imagem selecionada ($uri): ${e.message}", e)
            Result.failure(e)
        }
    }

    fun restoreDefaultBackground(context: Context): Boolean {
        val customFile = File(context.filesDir, CUSTOM_BG_FILENAME)
        if (customFile.exists()) {
            customFile.delete()
        }
        invalidateCache()
        return true
    }

    fun invalidateCache() {
        cachedBitmap?.recycle()
        cachedBitmap = null
    }

    private fun decodeSourceBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                // Obrigatório: Alocação em software e mutabilidade para desenhar em Canvas de software
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true

                val sampleSize = calculateInSampleSize(
                    info.size.width,
                    info.size.height,
                    TARGET_WIDTH,
                    TARGET_HEIGHT
                )
                if (sampleSize > 1) {
                    decoder.setTargetSampleSize(sampleSize)
                }
            }
        } else {
            // Fallback para API < 28 (Android 8.0/8.1)
            // 1. Leitura de orientação EXIF com InputStream dedicado
            val rotationDegrees = getExifRotation(context, uri)

            // 2. Leitura de dimensões com InputStream dedicado
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: throw IOException("Não foi possível abrir o arquivo da imagem.")

            val origW = boundsOptions.outWidth
            val origH = boundsOptions.outHeight
            if (origW <= 0 || origH <= 0) {
                throw IOException("Dimensões inválidas da imagem ($origW x $origH).")
            }

            val targetW = if (rotationDegrees == 90 || rotationDegrees == 270) TARGET_HEIGHT else TARGET_WIDTH
            val targetH = if (rotationDegrees == 90 || rotationDegrees == 270) TARGET_WIDTH else TARGET_HEIGHT
            val sampleSize = calculateInSampleSize(origW, origH, targetW, targetH)

            // 3. Decodificação com InputStream dedicado e inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IOException("Falha ao ler os pixels da imagem.")

            // 4. Correção de rotação se necessário
            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    sampledBitmap,
                    0, 0,
                    sampledBitmap.width, sampledBitmap.height,
                    matrix,
                    true
                )
                if (rotated != sampledBitmap) {
                    sampledBitmap.recycle()
                }
                rotated
            } else {
                sampledBitmap
            }
        }
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível extrair orientação EXIF: ${e.message}")
            0
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createFallbackBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(TARGET_WIDTH, TARGET_HEIGHT, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.WHITE)
        return bmp
    }
}
