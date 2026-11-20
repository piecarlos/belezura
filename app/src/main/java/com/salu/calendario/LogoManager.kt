package com.salu.calendario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

enum class LogoDisplayMode(val displayName: String, val description: String) {
    ORIGINAL("Original", "Preserva cores e transparência original"),
    REMOVE_WHITE("Sem fundo", "Mescla fundos brancos comuns via Multiply"),
    CIRCLE_BADGE("Redondo", "Recorte circular com fundo branco e borda sutil")
}

object LogoManager {
    private const val TAG = "LogoManager"
    private const val LOGO_FILENAME = "user_logo.png"
    const val MAX_LOGO_DIMENSION = 600

    @Volatile
    private var cachedLogo: Bitmap? = null

    fun getLogoBitmap(context: Context): Bitmap? {
        cachedLogo?.let {
            if (!it.isRecycled) return it
        }

        val logoFile = File(context.filesDir, LOGO_FILENAME)
        if (!logoFile.exists() || logoFile.length() <= 0) {
            return null
        }

        return try {
            val options = BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val loaded = BitmapFactory.decodeFile(logoFile.absolutePath, options)
            cachedLogo = loaded
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar logomarca: ${e.message}", e)
            null
        }
    }

    fun hasCustomLogo(context: Context): Boolean {
        val file = File(context.filesDir, LOGO_FILENAME)
        return file.exists() && file.length() > 0
    }

    fun saveLogoFromUri(context: Context, uri: Uri): Result<Unit> {
        return try {
            val srcBitmap = decodeSourceBitmap(context, uri)

            // Redimensionar mantendo proporção para caber em no máximo 600x600 px
            val origW = srcBitmap.width
            val origH = srcBitmap.height
            val scale = if (max(origW, origH) > MAX_LOGO_DIMENSION) {
                MAX_LOGO_DIMENSION.toFloat() / max(origW, origH).toFloat()
            } else {
                1.0f
            }

            val targetW = (origW * scale).roundToInt().coerceAtLeast(1)
            val targetH = (origH * scale).roundToInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                val scaled = Bitmap.createScaledBitmap(srcBitmap, targetW, targetH, true)
                if (scaled != srcBitmap) {
                    srcBitmap.recycle()
                }
                scaled
            } else {
                srcBitmap
            }

            // Salvar em filesDir/user_logo.png preservando canal alpha
            val logoFile = File(context.filesDir, LOGO_FILENAME)
            FileOutputStream(logoFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            invalidateCache()
            cachedLogo = scaledBitmap
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar e salvar logomarca ($uri): ${e.message}", e)
            Result.failure(e)
        }
    }

    fun clearLogo(context: Context): Boolean {
        val file = File(context.filesDir, LOGO_FILENAME)
        if (file.exists()) {
            file.delete()
        }
        invalidateCache()
        return true
    }

    fun invalidateCache() {
        cachedLogo?.recycle()
        cachedLogo = null
    }

    private fun decodeSourceBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                val maxDim = max(info.size.width, info.size.height)
                if (maxDim > MAX_LOGO_DIMENSION * 2) {
                    decoder.setTargetSampleSize(2)
                }
            }
        } else {
            // Fallback para API < 28
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: throw IOException("Não foi possível abrir o arquivo da imagem.")

            var inSampleSize = 1
            val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
            while ((maxDim / inSampleSize) > MAX_LOGO_DIMENSION * 2) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IOException("Falha ao decodificar a imagem da logomarca.")
        }
    }
}
