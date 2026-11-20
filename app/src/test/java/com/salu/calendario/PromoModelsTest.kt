package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoModelsTest {

    @Test
    fun testPresetsArePopulatedAndValid() {
        for (type in PromoTemplateType.values()) {
            val preset = PromoDraft.getPreset(type)
            assertEquals(type, preset.templateType)
            assertTrue("Badge should not be blank for $type", preset.badgeText.isNotBlank())
            assertTrue("Title should not be blank for $type", preset.title.isNotBlank())
            assertTrue("Subtitle should not be blank for $type", preset.subtitle.isNotBlank())
            assertTrue("Footer CTA should not be blank for $type", preset.footerCta.isNotBlank())
            assertTrue("Items should not be empty for $type", preset.items.isNotEmpty())
            assertTrue("Items count should be <= 6 for $type", preset.items.size <= PromoDraft.MAX_ITEMS)
            assertTrue("showLogo should default to true", preset.showLogo)
        }
    }

    @Test
    fun testJsonRoundTrip() {
        val original = PromoDraft(
            templateType = PromoTemplateType.TABELA_PRECOS,
            badgeText = "SUPER PROMO",
            title = "Promoção de Sexta",
            subtitle = "Horários selecionados",
            items = listOf(
                PromoLineItem("Corte + Escova", "R$ 60,00"),
                PromoLineItem("Hidratação Profunda", "R$ 80,00"),
                PromoLineItem("Design Sobrancelhas", "R$ 35,00")
            ),
            footerCta = "Chama no WhatsApp!",
            showLogo = false
        )

        val jsonStr = original.toJson()
        val parsed = PromoDraft.fromJson(jsonStr)

        assertEquals(original.templateType, parsed.templateType)
        assertEquals(original.badgeText, parsed.badgeText)
        assertEquals(original.title, parsed.title)
        assertEquals(original.subtitle, parsed.subtitle)
        assertEquals(original.footerCta, parsed.footerCta)
        assertEquals(original.showLogo, parsed.showLogo)
        assertEquals(original.items.size, parsed.items.size)
        for (i in original.items.indices) {
            assertEquals(original.items[i].label, parsed.items[i].label)
            assertEquals(original.items[i].value, parsed.items[i].value)
        }
    }

    @Test
    fun testFromJsonFallbackOnEmptyOrCorruptedJson() {
        val fallbackEmpty = PromoDraft.fromJson("")
        assertNotNull(fallbackEmpty)
        assertEquals(PromoTemplateType.AGENDA_ABERTA, fallbackEmpty.templateType)
        assertTrue(fallbackEmpty.items.isNotEmpty())

        val fallbackCorrupt = PromoDraft.fromJson("{invalid-json:::")
        assertNotNull(fallbackCorrupt)
        assertEquals(PromoTemplateType.AGENDA_ABERTA, fallbackCorrupt.templateType)

        val fallbackPartial = PromoDraft.fromJson("{\"templateType\":\"AVISO_REGRAS\"}")
        assertEquals(PromoTemplateType.AVISO_REGRAS, fallbackPartial.templateType)
        assertEquals("COMUNICADO IMPORTANTE", fallbackPartial.badgeText)
        assertEquals("Avisos do Estúdio", fallbackPartial.title)
    }

    @Test
    fun testMaxItemsClampedAtSix() {
        val tooManyItems = (1..10).map { PromoLineItem("Item $it", "Valor $it") }
        val draft = PromoDraft(
            templateType = PromoTemplateType.AGENDA_ABERTA,
            items = tooManyItems
        )
        val json = draft.toJson()
        val parsed = PromoDraft.fromJson(json)

        assertEquals(PromoDraft.MAX_ITEMS, parsed.items.size)
        assertEquals("Item 1", parsed.items[0].label)
        assertEquals("Item 6", parsed.items[5].label)
    }
}
