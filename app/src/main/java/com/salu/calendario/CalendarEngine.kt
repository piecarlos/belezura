package com.salu.calendario

import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object CalendarEngine {
    val DEFAULT_YEAR_MONTH: YearMonth = YearMonth.of(2026, 12)

    val MONTH_NAMES_PT = listOf(
        "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL",
        "MAIO", "JUNHO", "JULHO", "AGOSTO",
        "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
    )

    val WEEKDAY_HEADERS = listOf("D", "S", "T", "Q", "Q", "S", "S")

    fun getMonthDisplayName(yearMonth: YearMonth): String {
        val monthIdx = yearMonth.monthValue - 1
        return if (monthIdx in MONTH_NAMES_PT.indices) {
            MONTH_NAMES_PT[monthIdx]
        } else {
            yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).uppercase()
        }
    }

    fun getHeaderTitle(yearMonth: YearMonth): String {
        val monthName = getMonthDisplayName(yearMonth).lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
        return "$monthName ${yearMonth.year}"
    }

    fun calculateStartOffset(yearMonth: YearMonth): Int {
        // DayOfWeek: MONDAY(1) .. SUNDAY(7). In Brazil: Sunday is 0, Mon 1.. Sat 6
        return yearMonth.atDay(1).dayOfWeek.value % 7
    }

    fun calculateRowCount(yearMonth: YearMonth): Int {
        val startOffset = calculateStartOffset(yearMonth)
        val daysInMonth = yearMonth.lengthOfMonth()
        return (startOffset + daysInMonth + 6) / 7
    }

    fun generateCells(yearMonth: YearMonth, savedCells: Map<Int, CellData>): List<CellData> {
        val startOffset = calculateStartOffset(yearMonth)
        val daysInMonth = yearMonth.lengthOfMonth()
        val rowCount = (startOffset + daysInMonth + 6) / 7
        val totalCells = rowCount * 7

        val result = ArrayList<CellData>(totalCells)
        for (slotIndex in 0 until totalCells) {
            val day = slotIndex - startOffset + 1
            val dayNumber: Int? = if (day in 1..daysInMonth) day else null
            val saved = savedCells[slotIndex]

            result.add(
                CellData(
                    slotIndex = slotIndex,
                    dayNumber = dayNumber,
                    hasRedX = saved?.hasRedX ?: false,
                    names = saved?.names ?: emptyList()
                )
            )
        }
        return result
    }
}
