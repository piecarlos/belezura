package com.salu.calendario

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun generateCalendarBitmap(
        context: Context,
        yearMonth: YearMonth,
        cells: List<CellData>,
        layoutConfig: LayoutConfig
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            CalendarRenderer.VIRTUAL_WIDTH.toInt(),
            CalendarRenderer.VIRTUAL_HEIGHT.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        CalendarRenderer.drawCalendar(
            canvas = canvas,
            context = context,
            yearMonth = yearMonth,
            cells = cells,
            layoutConfig = layoutConfig,
            isEditMode = false,
            selectedElement = EditableElement.NONE
        )
        return bitmap
    }

    fun saveToGallery(
        context: Context,
        yearMonth: YearMonth,
        cells: List<CellData>,
        layoutConfig: LayoutConfig
    ): Result<String> {
        return try {
            val bitmap = generateCalendarBitmap(context, yearMonth, cells, layoutConfig)
            val monthName = CalendarEngine.getMonthDisplayName(yearMonth)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "Agenda_${monthName}_${yearMonth.year}_$timestamp.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AgendaSalu")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(Exception("Falha ao criar entrada no MediaStore"))

                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: return Result.failure(Exception("Falha ao abrir stream de saída"))

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success("Imagem salva em Imagens/AgendaSalu/$filename")
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "AgendaSalu")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val destFile = File(targetDir, filename)
                FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("image/png"),
                    null
                )
                Result.success("Imagem salva em ${destFile.absolutePath}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun shareCalendar(
        context: Context,
        yearMonth: YearMonth,
        cells: List<CellData>,
        layoutConfig: LayoutConfig
    ): Result<Unit> {
        return try {
            val bitmap = generateCalendarBitmap(context, yearMonth, cells, layoutConfig)
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val exportFile = File(exportDir, "agenda_${yearMonth.year}_${yearMonth.monthValue}.png")
            FileOutputStream(exportFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = android.content.ClipData.newUri(context.contentResolver, "Agenda", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Agenda")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
