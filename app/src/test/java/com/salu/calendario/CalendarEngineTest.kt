package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class CalendarEngineTest {

    @Test
    fun testFebruaryNonLeapYear_2027() {
        val ym = YearMonth.of(2027, 2)
        assertEquals(28, ym.lengthOfMonth())
        assertFalse(ym.isLeapYear)

        // Feb 1, 2027 is Monday -> in BR Sunday=0, Monday=1
        val startOffset = CalendarEngine.calculateStartOffset(ym)
        assertEquals(1, startOffset)

        val rowCount = CalendarEngine.calculateRowCount(ym)
        assertEquals(5, rowCount)

        val cells = CalendarEngine.generateCells(ym, emptyMap())
        assertEquals(35, cells.size)

        // Slot 0 should be null (empty before day 1)
        assertNull(cells[0].dayNumber)
        // Slot 1 should be Day 1
        assertEquals(1, cells[1].dayNumber)
        // Slot 28 should be Day 28
        assertEquals(28, cells[28].dayNumber)
        // Slot 29..34 should be null
        assertNull(cells[29].dayNumber)
        assertNull(cells[34].dayNumber)
    }

    @Test
    fun testFebruaryLeapYear_2028() {
        val ym = YearMonth.of(2028, 2)
        assertEquals(29, ym.lengthOfMonth())
        assertTrue(ym.isLeapYear)

        // Feb 1, 2028 is Tuesday -> offset = 2
        val startOffset = CalendarEngine.calculateStartOffset(ym)
        assertEquals(2, startOffset)

        val rowCount = CalendarEngine.calculateRowCount(ym)
        assertEquals(5, rowCount)

        val cells = CalendarEngine.generateCells(ym, emptyMap())
        assertEquals(35, cells.size)

        // Day 29 should be at slot 2 + 29 - 1 = slot 30
        assertEquals(29, cells[30].dayNumber)
        assertNull(cells[31].dayNumber)
    }

    @Test
    fun testThirtyDaysMonth_April2027() {
        val ym = YearMonth.of(2027, 4)
        assertEquals(30, ym.lengthOfMonth())

        val rowCount = CalendarEngine.calculateRowCount(ym)
        assertEquals(5, rowCount)
    }

    @Test
    fun testThirtyOneDaysFiveRows_December2026() {
        val ym = YearMonth.of(2026, 12)
        assertEquals(31, ym.lengthOfMonth())

        // Dec 1, 2026 is Tuesday -> startOffset = 2
        val startOffset = CalendarEngine.calculateStartOffset(ym)
        assertEquals(2, startOffset)

        val rowCount = CalendarEngine.calculateRowCount(ym)
        assertEquals(5, rowCount)

        val cells = CalendarEngine.generateCells(ym, emptyMap())
        assertEquals(35, cells.size)
        assertEquals(1, cells[2].dayNumber)
        assertEquals(31, cells[32].dayNumber)
        assertNull(cells[33].dayNumber)
        assertNull(cells[34].dayNumber)
    }

    @Test
    fun testSixRowsMonth_May2027() {
        val ym = YearMonth.of(2027, 5)
        assertEquals(31, ym.lengthOfMonth())

        // May 1, 2027 is Saturday -> startOffset = 6
        val startOffset = CalendarEngine.calculateStartOffset(ym)
        assertEquals(6, startOffset)

        // (6 + 31 + 6) / 7 = 43 / 7 = 6 rows
        val rowCount = CalendarEngine.calculateRowCount(ym)
        assertEquals(6, rowCount)

        val cells = CalendarEngine.generateCells(ym, emptyMap())
        assertEquals(42, cells.size)

        // Day 1 at slot 6
        assertEquals(1, cells[6].dayNumber)
        // Day 31 at slot 6 + 31 - 1 = 36
        assertEquals(31, cells[36].dayNumber)
        // Slots 37..41 empty
        for (i in 37..41) {
            assertNull(cells[i].dayNumber)
        }
    }

    @Test
    fun testPreserveSavedCellsData() {
        val ym = YearMonth.of(2026, 12)
        val savedMap = mapOf(
            2 to CellData(slotIndex = 2, dayNumber = 1, hasRedX = true, names = listOf("Ana", "Beatriz")),
            0 to CellData(slotIndex = 0, dayNumber = null, hasRedX = true, names = emptyList())
        )

        val cells = CalendarEngine.generateCells(ym, savedMap)
        // Slot 0 (empty cell) should preserve hasRedX = true
        assertTrue(cells[0].hasRedX)
        assertNull(cells[0].dayNumber)

        // Slot 2 (Day 1)
        assertEquals(1, cells[2].dayNumber)
        assertTrue(cells[2].hasRedX)
        assertEquals(listOf("Ana", "Beatriz"), cells[2].names)

        // Other slots should default to false / empty
        assertFalse(cells[3].hasRedX)
        assertTrue(cells[3].names.isEmpty())
    }
}
