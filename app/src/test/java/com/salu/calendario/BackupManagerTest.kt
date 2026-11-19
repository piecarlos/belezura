package com.salu.calendario

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun testCreateAndParseBackupSuccessfully() {
        val layout = LayoutConfig(
            monthX = 500f,
            monthY = 120f,
            monthScale = 1.1f,
            monthFont = HeaderFontOption.CINZEL_BOLD,
            monthColor = 0xFF5E2326.toInt(),
            yearX = 500f,
            yearY = 230f,
            yearScale = 0.95f,
            yearFont = HeaderFontOption.FREDOKA_BOLD,
            yearColor = 0xFFE64886.toInt(),
            cardOffsetX = 15f,
            cardOffsetY = -20f,
            cardScale = 1.05f,
            cardAccentColor = 0xFFE89BAE.toInt()
        )

        val month1Json = """
            {
              "slots": [
                { "slot": 2, "day": 1, "hasRedX": false, "names": ["Milka Noiva", "Maria"] },
                { "slot": 3, "day": 2, "hasRedX": true, "names": [] }
              ]
            }
        """.trimIndent()

        val month2Json = """
            {
              "slots": [
                { "slot": 5, "day": 1, "hasRedX": true, "names": ["Fernanda"] }
              ]
            }
        """.trimIndent()

        val monthsMap = mapOf(
            "month_data_2026_12" to month1Json,
            "month_data_2027_01" to month2Json
        )

        // 1. Generate Backup JSON
        val backupJson = BackupManager.createBackupJson(layout.toJson(), monthsMap)
        assertNotNull(backupJson)
        assertTrue(backupJson.contains("\"schemaVersion\": 1"))
        assertTrue(backupJson.contains("month_data_2026_12"))
        assertTrue(backupJson.contains("month_data_2027_01"))

        // 2. Parse and Validate Backup JSON
        val result = BackupManager.validateAndParseBackupJson(backupJson)
        assertTrue(result.isSuccess)

        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(2, parsed!!.monthsData.size)
        assertTrue(parsed.monthsData.containsKey("month_data_2026_12"))
        assertTrue(parsed.monthsData.containsKey("month_data_2027_01"))

        // Validate restored layout config
        val restoredLayout = LayoutConfig.fromJson(parsed.layoutConfigJson)
        assertEquals(500f, restoredLayout.monthX, 0.01f)
        assertEquals(120f, restoredLayout.monthY, 0.01f)
        assertEquals(15f, restoredLayout.cardOffsetX, 0.01f)
        assertEquals(-20f, restoredLayout.cardOffsetY, 0.01f)
        assertEquals(1.05f, restoredLayout.cardScale, 0.01f)
    }

    @Test
    fun testRejectInvalidOrCorruptedJson() {
        // Empty or null string
        assertTrue(BackupManager.validateAndParseBackupJson("").isFailure)
        assertTrue(BackupManager.validateAndParseBackupJson(null).isFailure)
        assertTrue(BackupManager.validateAndParseBackupJson("   ").isFailure)

        // Corrupted JSON syntax
        val brokenJson = "{ schemaVersion: 1, months: { broken "
        assertTrue(BackupManager.validateAndParseBackupJson(brokenJson).isFailure)

        // Missing schemaVersion
        val missingSchema = """{ "months": {} }"""
        assertTrue(BackupManager.validateAndParseBackupJson(missingSchema).isFailure)

        // Incompatible schemaVersion (e.g. version 99)
        val wrongSchema = """{ "schemaVersion": 99, "months": {} }"""
        val result = BackupManager.validateAndParseBackupJson(wrongSchema)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("incompatível") == true)

        // Missing months object
        val missingMonths = """{ "schemaVersion": 1, "layoutConfig": {} }"""
        assertTrue(BackupManager.validateAndParseBackupJson(missingMonths).isFailure)
    }
}
