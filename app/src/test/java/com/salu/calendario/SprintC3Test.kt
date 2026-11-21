package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class SprintC3Test {

    @Test
    fun testClientNameHelperNormalizationAndDuplicateDetection() {
        assertEquals("maria", ClientNameHelper.normalize("  Maria  "))
        assertEquals("maria", ClientNameHelper.normalize("MÁRIA"))
        assertEquals("maria", ClientNameHelper.normalize("mária"))
        assertEquals("claudia", ClientNameHelper.normalize("Cláudia"))
        assertEquals("joao", ClientNameHelper.normalize("JOÃO"))

        assertTrue(ClientNameHelper.isDuplicate("Maria", "maria"))
        assertTrue(ClientNameHelper.isDuplicate("MÁRIA", "mária"))
        assertTrue(ClientNameHelper.isDuplicate("  Cláudia ", "claudia"))
        assertTrue(ClientNameHelper.isDuplicate("João", "joao"))

        assertFalse(ClientNameHelper.isDuplicate("Maria", "Mariana"))
        assertFalse(ClientNameHelper.isDuplicate("Maria", "Ana"))
        assertFalse(ClientNameHelper.isDuplicate("", "Maria"))
    }

    @Test
    fun testFindDuplicateClientInCurrentMonthAndSavedMonths() {
        val currentYm = YearMonth.of(2026, 12)
        val currentCells = listOf(
            CellData(slotIndex = 2, dayNumber = 1, names = listOf("Maria Silva")),
            CellData(slotIndex = 10, dayNumber = 9, names = listOf("Glória"))
        )

        val mockEntries = mapOf(
            "month_data_2026_11" to """
                {
                  "slots": [
                    { "slot": 5, "day": 4, "names": ["Fátima", "Juliana"] }
                  ]
                }
            """.trimIndent()
        )

        // 1. Matches in current month
        val dup1 = ClientSearchHelper.findDuplicateClient("maria silva", currentYm, currentCells, mockEntries)
        assertNotNull(dup1)
        assertEquals("Maria Silva", dup1?.name)
        assertEquals(currentYm, dup1?.yearMonth)
        assertEquals(1, dup1?.day)

        // 2. Matches in another saved month with accent variance
        val dup2 = ClientSearchHelper.findDuplicateClient("fatima", currentYm, currentCells, mockEntries)
        assertNotNull(dup2)
        assertEquals("Fátima", dup2?.name)
        assertEquals(YearMonth.of(2026, 11), dup2?.yearMonth)
        assertEquals(4, dup2?.day)

        // 3. No match
        val dupNone = ClientSearchHelper.findDuplicateClient("Renata", currentYm, currentCells, mockEntries)
        assertNull(dupNone)

        // 4. Short text (< 2 chars) ignored
        val dupShort = ClientSearchHelper.findDuplicateClient("M", currentYm, currentCells, mockEntries)
        assertNull(dupShort)
    }

    @Test
    fun testVisualSnapshotSerializationAndBackwardCompatibility() {
        val originalLayout = LayoutConfig(
            logoScale = 1.5f,
            logoOffsetX = 20f,
            logoOffsetY = -40f,
            monthColor = 0xFF123456.toInt()
        )
        val snapshot = VisualSnapshot(
            themeId = "black_friday_luxo",
            themeName = "Black Friday Luxo",
            customBackgroundUri = "content://media/external/images/1",
            layoutConfig = originalLayout
        )

        val jsonStr = snapshot.toJson()
        val parsed = VisualSnapshot.fromJson(jsonStr)

        assertNotNull(parsed)
        assertEquals("black_friday_luxo", parsed?.themeId)
        assertEquals("Black Friday Luxo", parsed?.themeName)
        assertEquals("content://media/external/images/1", parsed?.customBackgroundUri)
        assertEquals(1.5f, parsed?.layoutConfig?.logoScale)
        assertEquals(20f, parsed?.layoutConfig?.logoOffsetX)
        assertEquals(-40f, parsed?.layoutConfig?.logoOffsetY)
        assertEquals(0xFF123456.toInt(), parsed?.layoutConfig?.monthColor)

        // Retrocompatibility test with empty / legacy JSON
        val minimalJson = "{}"
        val parsedMinimal = VisualSnapshot.fromJson(minimalJson)
        assertNotNull(parsedMinimal)
        assertEquals("floral_default", parsedMinimal?.themeId)
        assertEquals("Floral Clássico", parsedMinimal?.themeName)
        assertNull(parsedMinimal?.customBackgroundUri)
        assertEquals(1.0f, parsedMinimal?.layoutConfig?.logoScale)
    }

    @Test
    fun testThemeCatalogHasIsNewFlags() {
        val newThemes = listOf(
            "black_friday_luxo",
            "natal_classico",
            "natal_clean",
            "natal_minimal",
            "ano_novo_glam",
            "carnaval_glam"
        )
        for (id in newThemes) {
            val theme = ThemeCatalog.getThemeById(id)
            assertNotNull("Tema $id deve existir no catálogo", theme)
            assertEquals("2026-11-20", theme!!.addedDate)
            assertTrue("Tema $id deve ter isNew = true hoje", theme.isNew)
        }

        val defaultTheme = ThemeCatalog.getThemeById("floral_default")
        assertNotNull(defaultTheme)
        assertNull(defaultTheme!!.addedDate)
        assertFalse("Tema padrão não deve ser isNew", defaultTheme.isNew)
    }

    @Test
    fun testIsThemeNewExpirationRule() {
        // Base calendar: 2026-11-20 at 12:00:00
        val baseCal = java.util.Calendar.getInstance().apply {
            clear()
            set(2026, java.util.Calendar.NOVEMBER, 20, 12, 0, 0)
        }
        val addedTime = baseCal.timeInMillis

        // 1. addedDate null or blank -> false
        assertFalse(isThemeNew(null, nowMillis = addedTime))
        assertFalse(isThemeNew("", nowMillis = addedTime))
        assertFalse(isThemeNew("   ", nowMillis = addedTime))

        // 2. Exactly on the day of addition -> true
        assertTrue(isThemeNew("2026-11-20", nowMillis = addedTime))

        // 3. Within 30 days (e.g. 15 days later) -> true
        val day15Cal = java.util.Calendar.getInstance().apply {
            clear()
            set(2026, java.util.Calendar.DECEMBER, 5, 12, 0, 0)
        }
        assertTrue(isThemeNew("2026-11-20", nowMillis = day15Cal.timeInMillis))

        // 4. Exactly at 29 days 23h -> true
        val day29Cal = java.util.Calendar.getInstance().apply {
            timeInMillis = addedTime + (29L * 24L * 60L * 60L * 1000L)
        }
        assertTrue(isThemeNew("2026-11-20", nowMillis = day29Cal.timeInMillis))

        // 5. After 30 days (e.g. 30 days and 1 hour later) -> false
        val day30Cal = java.util.Calendar.getInstance().apply {
            timeInMillis = addedTime + (30L * 24L * 60L * 60L * 1000L) + 3600000L
        }
        assertFalse(isThemeNew("2026-11-20", nowMillis = day30Cal.timeInMillis))

        // 6. 60 days later -> false
        val day60Cal = java.util.Calendar.getInstance().apply {
            timeInMillis = addedTime + (60L * 24L * 60L * 60L * 1000L)
        }
        assertFalse(isThemeNew("2026-11-20", nowMillis = day60Cal.timeInMillis))

        // 7. Malformed dates -> false (safe, no exceptions)
        assertFalse(isThemeNew("2026-13-01", nowMillis = addedTime))
        assertFalse(isThemeNew("invalid-date", nowMillis = addedTime))
        assertFalse(isThemeNew("2026/11/20", nowMillis = addedTime))
        assertFalse(isThemeNew("2026-11", nowMillis = addedTime))

        // 8. Slight future skew (e.g. 6 hours before midnight of release date) -> true (treated safely)
        val slightlyBefore = java.util.Calendar.getInstance().apply {
            clear()
            set(2026, java.util.Calendar.NOVEMBER, 19, 20, 0, 0)
        }
        assertTrue(isThemeNew("2026-11-20", nowMillis = slightlyBefore.timeInMillis))
    }
}
