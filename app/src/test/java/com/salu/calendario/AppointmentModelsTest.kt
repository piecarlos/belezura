package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppointmentModelsTest {

    @Test
    fun testFormatBrl() {
        assertEquals("R$ 0,00", formatBrl(0L))
        assertEquals("R$ 150,00", formatBrl(15000L))
        assertEquals("R$ 650,00", formatBrl(65000L))
        assertEquals("R$ 1.234,56", formatBrl(123456L))
        assertEquals("-R$ 50,00", formatBrl(-5000L))
    }

    @Test
    fun testParseBrlToCents() {
        assertEquals(0L, parseBrlToCents(""))
        assertEquals(15000L, parseBrlToCents("150"))
        assertEquals(15000L, parseBrlToCents("150,00"))
        assertEquals(15000L, parseBrlToCents("R$ 150,00"))
        assertEquals(65000L, parseBrlToCents("R$ 650,00"))
        assertEquals(123456L, parseBrlToCents("1.234,56"))
        assertEquals(65000L, parseBrlToCents("650.00"))
        assertEquals(4550L, parseBrlToCents("45,5"))
    }

    @Test
    fun testAppointmentDetailCalculations() {
        // Case 1: Scheduled with deposit
        val detail1 = AppointmentDetail(
            service = "Maquiagem Social",
            priceCents = 15000L,
            depositCents = 5000L,
            time = "14:00",
            status = AppointmentStatus.SCHEDULED
        )
        assertEquals(10000L, detail1.remainingCents)
        assertEquals(5000L, detail1.receivedCents)
        assertEquals(10000L, detail1.pendingCents)

        // Case 2: Paid in full
        val detail2 = AppointmentDetail(
            service = "Noiva",
            priceCents = 65000L,
            depositCents = 20000L,
            status = AppointmentStatus.PAID
        )
        assertEquals(45000L, detail2.remainingCents)
        assertEquals(65000L, detail2.receivedCents)
        assertEquals(0L, detail2.pendingCents)

        // Case 3: Zero price
        val detail3 = AppointmentDetail(
            service = "Retoque",
            priceCents = 0L,
            depositCents = 0L
        )
        assertEquals(0L, detail3.remainingCents)
        assertEquals(0L, detail3.receivedCents)
        assertEquals(0L, detail3.pendingCents)
    }

    @Test
    fun testAppointmentDetailJsonRoundTrip() {
        val original = AppointmentDetail(
            service = "Penteado + Make",
            priceCents = 27000L,
            depositCents = 10000L,
            time = "16:30",
            phone = "11977778888",
            paymentMethod = PaymentMethod.CARD,
            status = AppointmentStatus.CONFIRMED,
            notes = "Cliente prefere cabelo preso"
        )

        val json = original.toJson()
        val restored = AppointmentDetail.fromJson(json)

        assertEquals(original.service, restored.service)
        assertEquals(original.priceCents, restored.priceCents)
        assertEquals(original.depositCents, restored.depositCents)
        assertEquals(original.time, restored.time)
        assertEquals(original.phone, restored.phone)
        assertEquals(original.paymentMethod, restored.paymentMethod)
        assertEquals(original.status, restored.status)
        assertEquals(original.notes, restored.notes)
    }

    @Test
    fun testStudioFinanceConfigJsonRoundTrip() {
        val original = StudioFinanceConfig(
            pixKey = "11988887777",
            pixHolderName = "Jéssica Salú",
            hideFinanceValues = true
        )

        val json = original.toJson()
        val restored = StudioFinanceConfig.fromJson(json)

        assertEquals(original.pixKey, restored.pixKey)
        assertEquals(original.pixHolderName, restored.pixHolderName)
        assertEquals(original.hideFinanceValues, restored.hideFinanceValues)
    }

    @Test
    fun testAppointmentKeyNormalization() {
        val key1 = appointmentKey(2026, 12, 15, "  Milka Noiva  ")
        assertEquals("2026_12_15_milka noiva", key1)

        val key2 = appointmentKey(2026, 11, 20, "Glória Maria")
        assertEquals("2026_11_20_gloria maria", key2)

        val key3 = appointmentKey(2026, 12, 15, "MILKA NOIVA")
        assertEquals(key1, key3)
    }

    @Test
    fun testCalculateMonthlyFinanceSummary() {
        val clients = listOf(
            Pair(5, "Maria"),
            Pair(12, "Jessica"),
            Pair(15, "Amanda"),
            Pair(20, "Sem Preco")
        )

        val detailsMap = mapOf(
            appointmentKey(2026, 12, 5, "Maria") to AppointmentDetail(
                service = "Maquiagem",
                priceCents = 15000L,
                depositCents = 5000L,
                status = AppointmentStatus.SCHEDULED
            ),
            appointmentKey(2026, 12, 12, "Jessica") to AppointmentDetail(
                service = "Noiva",
                priceCents = 65000L,
                depositCents = 20000L,
                status = AppointmentStatus.CONFIRMED
            ),
            appointmentKey(2026, 12, 15, "Amanda") to AppointmentDetail(
                service = "Sobrancelha",
                priceCents = 4500L,
                depositCents = 4500L,
                status = AppointmentStatus.PAID
            )
        )

        val summary = calculateMonthlyFinanceSummary(2026, 12, clients, detailsMap)

        assertEquals(4, summary.totalCount)
        assertEquals(3, summary.pricedCount)
        assertEquals(84500L, summary.totalExpectedCents) // 15000 + 65000 + 4500
        assertEquals(29500L, summary.receivedCents) // 5000 + 20000 + 4500
        assertEquals(55000L, summary.pendingCents) // 10000 + 45000 + 0
        assertEquals(28166L, summary.averageTicketCents) // 84500 / 3 = 28166
    }
}
