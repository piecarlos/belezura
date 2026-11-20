package com.salu.calendario

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
    val sourceType: ThemeSourceType = ThemeSourceType.BUILTIN,
    val accentHex: String = "#E64886",
    val description: String = "",
    val drawableResId: Int? = null,
    val palette: ThemePalette = ThemePalette(
        monthColorHex = "#5E2326",
        yearColorHex = "#E64886",
        calendarAccentHex = "#E89BAE"
    )
)

interface ThemeRepository {
    fun getThemes(): List<ThemeItem>
    fun getThemeById(id: String): ThemeItem?
    fun getThemesByCategory(category: ThemeCategory): List<ThemeItem>
}
