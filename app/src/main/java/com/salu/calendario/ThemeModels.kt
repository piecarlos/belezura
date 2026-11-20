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

data class ThemeItem(
    val id: String,
    val name: String,
    val category: ThemeCategory,
    val seasonalTag: String? = null,
    val isPro: Boolean = false,
    val sourceType: ThemeSourceType = ThemeSourceType.BUILTIN,
    val accentHex: String = "#E64886",
    val description: String = ""
)

interface ThemeRepository {
    fun getThemes(): List<ThemeItem>
    fun getThemeById(id: String): ThemeItem?
    fun getThemesByCategory(category: ThemeCategory): List<ThemeItem>
}
