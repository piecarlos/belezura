package com.salu.calendario

import java.util.Calendar

enum class ThemeCategory(val displayName: String) {
    ALL("Todos"),
    SEASONAL("Em Alta / Datas"),
    FLORAL("Florais"),
    MINIMALIST("Minimalistas"),
    LUXURY("Luxo"),
    COMMEMORATIVE("Comemorativos")
}

enum class ThemeSourceType {
    BUILTIN,
    EMBEDDED,
    PROCEDURAL,
    CUSTOM
}

data class ThemePalette(
    val monthColorHex: String,
    val yearColorHex: String,
    val calendarAccentHex: String,
    val backgroundBaseHex: String = "#FFFFFF",
    val suggestedMonthFont: HeaderFontOption? = null,
    val suggestedYearFont: HeaderFontOption? = null,
    val defaultCalendarScale: Float = 1.0f,
    val defaultMonthScale: Float? = null,
    val defaultYearScale: Float? = null
)

data class ThemeItem(
    val id: String,
    val name: String,
    val category: ThemeCategory,
    val seasonalTag: String? = null,
    val isPro: Boolean = false,
    val addedDate: String? = null,
    val sourceType: ThemeSourceType = ThemeSourceType.BUILTIN,
    val accentHex: String = "#E64886",
    val description: String = "",
    val drawableResId: Int? = null,
    val palette: ThemePalette = ThemePalette(
        monthColorHex = "#5E2326",
        yearColorHex = "#E64886",
        calendarAccentHex = "#E89BAE"
    )
) {
    val isNew: Boolean get() = isThemeNew(addedDate)

    fun isNew(nowMillis: Long, windowDays: Int = 30): Boolean =
        isThemeNew(addedDate, nowMillis, windowDays)
}

fun isThemeNew(
    addedDate: String?,
    nowMillis: Long = System.currentTimeMillis(),
    windowDays: Int = 30
): Boolean {
    if (addedDate.isNullOrBlank()) return false
    return try {
        val parts = addedDate.split("-")
        if (parts.size != 3) return false
        val year = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val day = parts[2].toIntOrNull() ?: return false

        if (year < 2000 || month !in 1..12 || day !in 1..31) return false

        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val addedMillis = cal.timeInMillis
        val diffMillis = nowMillis - addedMillis
        val windowMillis = windowDays.toLong() * 24L * 60L * 60L * 1000L
        diffMillis >= -86_400_000L && diffMillis < windowMillis
    } catch (_: Exception) {
        false
    }
}

interface ThemeRepository {
    fun getThemes(): List<ThemeItem>
    fun getThemeById(id: String): ThemeItem?
    fun getThemesByCategory(category: ThemeCategory): List<ThemeItem>
}
