package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutConfigTest {

    @Test
    fun testDefaultValues() {
        val config = LayoutConfig.DEFAULT
        assertEquals("floral_default", config.selectedThemeId)
        assertFalse(config.logoEnabled)
        assertEquals("ORIGINAL", config.logoMode)
        assertEquals(1.0f, config.logoScale, 0.001f)
        assertEquals(0f, config.logoOffsetX, 0.001f)
        assertEquals(0f, config.logoOffsetY, 0.001f)
    }

    @Test
    fun testBackwardCompatibilityOldJson() {
        // Legacy JSON without any theme or logo fields
        val legacyJson = """
            {
                "monthX": 540.0,
                "monthY": 115.0,
                "monthOffsetX": 0.0,
                "monthOffsetY": 0.0,
                "monthScale": 1.2,
                "monthFont": "PLAYFAIR_DISPLAY_BOLD",
                "monthColor": "#5E2326",
                "yearX": 540.0,
                "yearY": 225.0,
                "yearOffsetX": 10.0,
                "yearOffsetY": -5.0,
                "yearScale": 0.9,
                "yearFont": "FREDOKA_BOLD",
                "yearColor": "#E64886",
                "cardLeft": 20.0,
                "cardRight": 1060.0,
                "cardTop": 278.0,
                "cardBottom": 1612.0,
                "cardScale": 1.0,
                "cardOffsetX": 0.0,
                "cardOffsetY": 0.0,
                "cardAccentColor": "#E89BAE"
            }
        """.trimIndent()

        val parsed = LayoutConfig.fromJson(legacyJson)
        assertEquals(1.2f, parsed.monthScale, 0.001f)
        assertEquals(HeaderFontOption.PLAYFAIR_DISPLAY_BOLD, parsed.monthFont)
        assertEquals(10f, parsed.yearOffsetX, 0.001f)
        assertEquals(-5f, parsed.yearOffsetY, 0.001f)
        // Fallback to default values for Phase A fields
        assertEquals("floral_default", parsed.selectedThemeId)
        assertFalse(parsed.logoEnabled)
        assertEquals("ORIGINAL", parsed.logoMode)
        assertEquals(1.0f, parsed.logoScale, 0.001f)
    }

    @Test
    fun testRoundTripWithPhaseAFields() {
        val original = LayoutConfig(
            selectedThemeId = "luxury_gold",
            logoEnabled = true,
            logoMode = "CIRCLE_BADGE",
            logoScale = 1.3f,
            logoOffsetX = 25f,
            logoOffsetY = -15f
        )

        val json = original.toJson()
        val parsed = LayoutConfig.fromJson(json)

        assertEquals("luxury_gold", parsed.selectedThemeId)
        assertTrue(parsed.logoEnabled)
        assertEquals("CIRCLE_BADGE", parsed.logoMode)
        assertEquals(1.3f, parsed.logoScale, 0.001f)
        assertEquals(25f, parsed.logoOffsetX, 0.001f)
        assertEquals(-15f, parsed.logoOffsetY, 0.001f)
    }

    @Test
    fun testEmptyOrInvalidJsonReturnsDefault() {
        val fromNull = LayoutConfig.fromJson(null)
        val fromBlank = LayoutConfig.fromJson("")
        val fromInvalid = LayoutConfig.fromJson("not-a-json")

        assertEquals(LayoutConfig.DEFAULT, fromNull)
        assertEquals(LayoutConfig.DEFAULT, fromBlank)
        assertEquals(LayoutConfig.DEFAULT, fromInvalid)
    }
}
