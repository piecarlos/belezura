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
    LOGO
}

enum class AppScreen {
    HOME,
    CALENDAR
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
