package com.salu.calendario

import android.content.Context
import android.net.Uri
import org.json.JSONObject

object BackupManager {
    const val SCHEMA_VERSION = 1

    data class ParsedBackup(
        val layoutConfigJson: String?,
        val monthsData: Map<String, String>
    )

    fun createBackupJson(
        layoutConfigJson: String?,
        monthsData: Map<String, String>
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("timestamp", System.currentTimeMillis())

        if (!layoutConfigJson.isNullOrBlank()) {
            root.put("layoutConfig", JSONObject(layoutConfigJson))
        } else {
            root.put("layoutConfig", JSONObject(LayoutConfig.DEFAULT.toJson()))
        }

        val monthsObj = JSONObject()
        for ((key, value) in monthsData) {
            try {
                monthsObj.put(key, JSONObject(value))
            } catch (_: Exception) {
                // Ignore malformed month entries
            }
        }
        root.put("months", monthsObj)

        return root.toString(2)
    }

    fun validateAndParseBackupJson(jsonString: String?): Result<ParsedBackup> {
        if (jsonString.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Arquivo de backup vazio ou inválido."))
        }
        return try {
            val root = JSONObject(jsonString)
            val schema = root.optInt("schemaVersion", -1)
            if (schema != SCHEMA_VERSION) {
                return Result.failure(IllegalArgumentException("Versão de schema incompatível: $schema (esperado: $SCHEMA_VERSION)"))
            }

            val layoutConfigJson = root.optJSONObject("layoutConfig")?.toString()
            val monthsObj = root.optJSONObject("months")
                ?: return Result.failure(IllegalArgumentException("Formato inválido: campo 'months' ausente."))

            val monthsMap = mutableMapOf<String, String>()
            val keys = monthsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.startsWith("month_data_")) {
                    val monthObj = monthsObj.getJSONObject(key)
                    monthsMap[key] = monthObj.toString()
                }
            }

            Result.success(ParsedBackup(layoutConfigJson, monthsMap))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Erro ao processar JSON de backup: ${e.message}", e))
        }
    }

    fun exportToUri(context: Context, uri: Uri): Result<Unit> {
        return try {
            val prefs = context.getSharedPreferences("salu_calendar_prefs", Context.MODE_PRIVATE)
            val allEntries = prefs.all
            val layoutJson = allEntries["layout_config"] as? String
            val monthsMap = mutableMapOf<String, String>()
            for ((key, value) in allEntries) {
                if (key.startsWith("month_data_") && value is String) {
                    monthsMap[key] = value
                }
            }

            val backupContent = createBackupJson(layoutJson, monthsMap)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(backupContent.toByteArray(Charsets.UTF_8))
            } ?: return Result.failure(Exception("Não foi possível abrir o arquivo para gravação."))

            prefs.edit().putLong("last_backup_at", System.currentTimeMillis()).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importFromUri(context: Context, uri: Uri): Result<Unit> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return Result.failure(Exception("Não foi possível ler o arquivo selecionado."))

            val parseResult = validateAndParseBackupJson(content)
            parseResult.fold(
                onSuccess = { backup ->
                    val prefs = context.getSharedPreferences("salu_calendar_prefs", Context.MODE_PRIVATE)
                    val editor = prefs.edit()

                    // Clear existing months & layout
                    val existingKeys = prefs.all.keys
                    for (k in existingKeys) {
                        if (k.startsWith("month_data_") || k == "layout_config") {
                            editor.remove(k)
                        }
                    }

                    backup.layoutConfigJson?.let {
                        editor.putString("layout_config", it)
                    }
                    for ((k, v) in backup.monthsData) {
                        editor.putString(k, v)
                    }
                    editor.apply()

                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLastBackupTimeFormatted(context: Context): String? {
        val prefs = context.getSharedPreferences("salu_calendar_prefs", Context.MODE_PRIVATE)
        val timestamp = prefs.getLong("last_backup_at", 0L)
        if (timestamp <= 0L) return null
        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
