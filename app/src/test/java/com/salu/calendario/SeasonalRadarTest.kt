package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalRadarTest {

    @Test
    fun testBlackFridayDetectedWithin20Days() {
        // In 2026, Black Friday is Nov 27
        val testDate = LocalDate.of(2026, 11, 19)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNotNull(campaign)
        assertEquals("black_friday", campaign?.id)
        assertEquals(8L, campaign?.daysRemaining)
        assertTrue(campaign?.title?.contains("Black Friday") == true)
    }

    @Test
    fun testMothersDayDetected() {
        // In 2026, May 10 is Mother's Day (2nd Sunday of May)
        val testDate = LocalDate.of(2026, 5, 2)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNotNull(campaign)
        assertEquals("mothers_day", campaign?.id)
        assertEquals(8L, campaign?.daysRemaining)
    }

    @Test
    fun testPinkOctoberActiveDuringMonth() {
        val testDate = LocalDate.of(2026, 10, 15)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNotNull(campaign)
        assertEquals("pink_october", campaign?.id)
        assertEquals(0L, campaign?.daysRemaining)
        assertTrue(campaign?.title?.contains("Em alta agora: Outubro Rosa") == true)
    }

    @Test
    fun testChristmasAndNewYearActive() {
        val testDate = LocalDate.of(2026, 12, 20)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNotNull(campaign)
        assertEquals("christmas_newyear", campaign?.id)
        assertEquals(0L, campaign?.daysRemaining)
    }

    @Test
    fun testOffSeasonReturnsNull() {
        // April 15 has no commercial beauty date within 20 days
        val testDate = LocalDate.of(2026, 4, 15)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNull(campaign)
    }

    @Test
    fun testSummerActiveInJanuary() {
        val testDate = LocalDate.of(2026, 1, 10)
        val campaign = SeasonalRadar.getActiveCampaign(testDate)
        assertNotNull(campaign)
        assertEquals("summer", campaign?.id)
        assertEquals(0L, campaign?.daysRemaining)
        assertNotNull(campaign?.promoPreset)
        assertEquals("VERÃO & FÉRIAS", campaign?.promoPreset?.badgeText)
    }

    @Test
    fun testAllCampaignIdsReturnValidPromoPreset() {
        val campaignIds = listOf(
            "summer",
            "carnival",
            "womens_day",
            "mothers_day",
            "valentines_day",
            "sao_joao",
            "client_day",
            "pink_october",
            "black_friday",
            "christmas_newyear"
        )
        for (id in campaignIds) {
            val preset = SeasonalRadar.getPromoPresetForCampaign(id)
            assertNotNull("Preset for $id should not be null", preset)
            assertTrue("Badge should not be blank for $id", preset.badgeText.isNotBlank())
            assertTrue("Title should not be blank for $id", preset.title.isNotBlank())
            assertTrue("Subtitle should not be blank for $id", preset.subtitle.isNotBlank())
            assertTrue("Items should not be empty for $id", preset.items.isNotEmpty())
            assertTrue("Footer CTA should not be blank for $id", preset.footerCta.isNotBlank())
        }
    }
}
