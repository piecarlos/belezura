package com.salu.calendario

import java.time.YearMonth

data class CellData(
    val slotIndex: Int,
    val dayNumber: Int?,
    val hasRedX: Boolean = false,
    val names: List<String> = emptyList()
)

data class MonthData(
    val yearMonthStr: String,
    val cells: Map<Int, CellData> = emptyMap()
)

enum class EditableElement {
    NONE,
    MONTH,
    YEAR,
    GRID_CARD,
    LOGO,
    FOOTER_MESSAGE
}

enum class AppScreen {
    HOME,
    CALENDAR,
    PROMO_EDITOR
}

data class MonthSummary(
    val yearMonth: YearMonth,
    val namesCount: Int,
    val redXCount: Int
)

data class ClientSearchResult(
    val name: String,
    val yearMonth: YearMonth,
    val day: Int,
    val slotIndex: Int
)

object ClientNameHelper {
    fun normalize(name: String): String {
        val trimmed = name.trim().lowercase(java.util.Locale.ROOT)
        val normalized = java.text.Normalizer.normalize(trimmed, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun isDuplicate(nameA: String, nameB: String): Boolean {
        val normA = normalize(nameA)
        val normB = normalize(nameB)
        return normA.isNotEmpty() && normA == normB
    }
}

data class VisualSnapshot(
    val themeId: String,
    val themeName: String,
    val customBackgroundUri: String? = null,
    val layoutConfig: LayoutConfig = LayoutConfig()
) {
    fun toJson(): String {
        val obj = org.json.JSONObject()
        obj.put("themeId", themeId)
        obj.put("themeName", themeName)
        if (customBackgroundUri != null) {
            obj.put("customBackgroundUri", customBackgroundUri)
        }
        obj.put("layoutConfig", layoutConfig.toJson())
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): VisualSnapshot? {
            return try {
                val obj = org.json.JSONObject(jsonStr)
                val themeId = obj.optString("themeId", "floral_default")
                val themeName = obj.optString("themeName", "Floral Clássico")
                val customBackgroundUri = if (obj.has("customBackgroundUri") && !obj.isNull("customBackgroundUri")) {
                    obj.optString("customBackgroundUri")
                } else null
                val layoutConfig = if (obj.has("layoutConfig")) {
                    LayoutConfig.fromJson(obj.optString("layoutConfig"))
                } else {
                    LayoutConfig()
                }
                VisualSnapshot(
                    themeId = themeId,
                    themeName = themeName,
                    customBackgroundUri = customBackgroundUri,
                    layoutConfig = layoutConfig
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

