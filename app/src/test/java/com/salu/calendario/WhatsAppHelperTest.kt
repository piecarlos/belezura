package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppHelperTest {

    @Test
    fun testFormatClientDatePtBr() {
        // 15/12/2026 is a Tuesday (terça-feira)
        val dateTue = WhatsAppHelper.formatClientDate(day = 15, month = 12, year = 2026)
        assertEquals("15/12 (terça-feira)", dateTue)

        // 20/11/2026 is a Friday (sexta-feira)
        val dateFri = WhatsAppHelper.formatClientDate(day = 20, month = 11, year = 2026)
        assertEquals("20/11 (sexta-feira)", dateFri)

        // 21/11/2026 is a Saturday (sábado)
        val dateSat = WhatsAppHelper.formatClientDate(day = 21, month = 11, year = 2026)
        assertEquals("21/11 (sábado)", dateSat)

        // 22/11/2026 is a Sunday (domingo)
        val dateSun = WhatsAppHelper.formatClientDate(day = 22, month = 11, year = 2026)
        assertEquals("22/11 (domingo)", dateSun)
    }

    @Test
    fun testBuildConfirmationMessage() {
        val msg = WhatsAppHelper.buildClientMessage(
            clientName = "  Milka Noiva  ",
            day = 15,
            month = 12,
            year = 2026,
            template = WhatsAppTemplateType.CONFIRMACAO
        )

        assertTrue(msg.contains("Olá, Milka Noiva!"))
        assertFalse(msg.contains("  Milka Noiva  "))
        assertTrue(msg.contains("15/12 (terça-feira)"))
        assertTrue(msg.contains("confirmar o seu agendamento"))
    }

    @Test
    fun testBuildReminderMessage() {
        val msg = WhatsAppHelper.buildClientMessage(
            clientName = "Fernanda",
            day = 20,
            month = 11,
            year = 2026,
            template = WhatsAppTemplateType.LEMBRETE
        )

        assertTrue(msg.contains("Oi, Fernanda!"))
        assertTrue(msg.contains("20/11 (sexta-feira)"))
        assertTrue(msg.contains("lembrar do seu atendimento"))
    }

    @Test
    fun testBuildPostCareMessage() {
        val msg = WhatsAppHelper.buildClientMessage(
            clientName = "Juliana Silva",
            day = 18,
            month = 11,
            year = 2026,
            template = WhatsAppTemplateType.POS_ATENDIMENTO
        )

        assertTrue(msg.contains("Olá, Juliana Silva!"))
        assertTrue(msg.contains("18/11 (quarta-feira)"))
        assertTrue(msg.contains("agradecer pela confiança"))
    }

    @Test
    fun testAllTemplateTypesProduceNonEmptyAndDistinctMessages() {
        val client = "Renata"
        val day = 10
        val month = 5
        val year = 2026

        val messages = WhatsAppTemplateType.values().map { template ->
            val msg = WhatsAppHelper.buildClientMessage(client, day, month, year, template)
            assertTrue("Message for $template should not be blank", msg.isNotBlank())
            assertTrue(msg.contains("Renata"))
            msg
        }

        assertEquals(4, messages.distinct().size)
    }

    @Test
    fun testBuildResumoPixMessage() {
        val detail = AppointmentDetail(
            service = "Maquiagem Social",
            priceCents = 15000L,
            depositCents = 5000L,
            time = "14:30",
            phone = "11988887777",
            paymentMethod = PaymentMethod.PIX,
            status = AppointmentStatus.CONFIRMED
        )
        val finance = StudioFinanceConfig(
            pixKey = "11988887777",
            pixHolderName = "Jessica Salú",
            hideFinanceValues = false
        )

        val msg = WhatsAppHelper.buildClientMessage(
            clientName = "Amanda",
            day = 12,
            month = 12,
            year = 2026,
            template = WhatsAppTemplateType.RESUMO_PIX,
            detail = detail,
            financeConfig = finance
        )

        assertTrue(msg.contains("*Amanda*"))
        assertTrue(msg.contains("14:30"))
        assertTrue(msg.contains("Maquiagem Social"))
        assertTrue(msg.contains("R$ 150,00"))
        assertTrue(msg.contains("R$ 50,00"))
        assertTrue(msg.contains("R$ 100,00"))
        assertTrue(msg.contains("`11988887777`"))
        assertTrue(msg.contains("(Jessica Salú)"))
    }
}
