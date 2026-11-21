package com.salu.calendario

import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class PaymentMethod(val label: String) {
    PIX("Pix"),
    CARD("Cartão"),
    CASH("Dinheiro"),
    OTHER("Outro")
}

enum class AppointmentStatus(val label: String) {
    SCHEDULED("Agendado"),
    CONFIRMED("Confirmado"),
    DONE("Concluído"),
    PAID("Pago")
}

data class AppointmentDetail(
    val service: String = "",
    val priceCents: Long = 0L,
    val depositCents: Long = 0L,
    val time: String = "",
    val phone: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.PIX,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String = ""
) {
    val remainingCents: Long get() = (priceCents - depositCents).coerceAtLeast(0L)
    val receivedCents: Long get() = if (status == AppointmentStatus.PAID) priceCents else depositCents.coerceAtMost(priceCents)
    val pendingCents: Long get() = (priceCents - receivedCents).coerceAtLeast(0L)

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("service", service)
        obj.put("priceCents", priceCents)
        obj.put("depositCents", depositCents)
        obj.put("time", time)
        obj.put("phone", phone)
        obj.put("paymentMethod", paymentMethod.name)
        obj.put("status", status.name)
        obj.put("notes", notes)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): AppointmentDetail {
            val paymentMethod = try {
                PaymentMethod.valueOf(obj.optString("paymentMethod", PaymentMethod.PIX.name))
            } catch (_: Exception) {
                PaymentMethod.PIX
            }
            val status = try {
                AppointmentStatus.valueOf(obj.optString("status", AppointmentStatus.SCHEDULED.name))
            } catch (_: Exception) {
                AppointmentStatus.SCHEDULED
            }
            return AppointmentDetail(
                service = obj.optString("service", ""),
                priceCents = obj.optLong("priceCents", 0L),
                depositCents = obj.optLong("depositCents", 0L),
                time = obj.optString("time", ""),
                phone = obj.optString("phone", ""),
                paymentMethod = paymentMethod,
                status = status,
                notes = obj.optString("notes", "")
            )
        }
    }
}

fun formatBrl(cents: Long): String {
    val absCents = kotlin.math.abs(cents)
    val reais = absCents / 100
    val centavos = absCents % 100
    val sign = if (cents < 0) "-" else ""
    val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
    val df = DecimalFormat("#,##0", symbols)
    return "${sign}R$ ${df.format(reais)},${String.format("%02d", centavos)}"
}

fun parseBrlToCents(input: String): Long {
    val clean = input.trim()
        .replace("R$", "", ignoreCase = true)
        .replace(" ", "")
        .trim()
    if (clean.isEmpty()) return 0L

    return try {
        if (clean.contains(",") && clean.contains(".")) {
            val normalized = clean.replace(".", "").replace(",", ".")
            val value = normalized.toDouble()
            (value * 100).toLong().coerceAtLeast(0L)
        } else if (clean.contains(",")) {
            val parts = clean.split(",")
            val reais = parts[0].toLongOrNull() ?: 0L
            val centavosStr = if (parts.size > 1) parts[1].padEnd(2, '0').take(2) else "00"
            val centavos = centavosStr.toLongOrNull() ?: 0L
            (reais * 100 + centavos).coerceAtLeast(0L)
        } else if (clean.contains(".")) {
            val parts = clean.split(".")
            val reais = parts[0].toLongOrNull() ?: 0L
            val centavosStr = if (parts.size > 1) parts[1].padEnd(2, '0').take(2) else "00"
            val centavos = centavosStr.toLongOrNull() ?: 0L
            (reais * 100 + centavos).coerceAtLeast(0L)
        } else {
            val reais = clean.toLongOrNull() ?: 0L
            (reais * 100).coerceAtLeast(0L)
        }
    } catch (_: Exception) {
        0L
    }
}

data class StudioFinanceConfig(
    val pixKey: String = "",
    val pixHolderName: String = "",
    val hideFinanceValues: Boolean = false
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("pixKey", pixKey)
        obj.put("pixHolderName", pixHolderName)
        obj.put("hideFinanceValues", hideFinanceValues)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): StudioFinanceConfig {
            return StudioFinanceConfig(
                pixKey = obj.optString("pixKey", ""),
                pixHolderName = obj.optString("pixHolderName", ""),
                hideFinanceValues = obj.optBoolean("hideFinanceValues", false)
            )
        }
    }
}

data class ServicePreset(val name: String, val defaultPriceCents: Long) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            ServicePreset("Maquiagem Social", 15000L),
            ServicePreset("Noiva", 65000L),
            ServicePreset("Sobrancelha", 4500L),
            ServicePreset("Cílios", 12000L),
            ServicePreset("Penteado", 14000L),
            ServicePreset("Combo Make + Penteado", 27000L)
        )
    }
}

data class MonthlyFinanceSummary(
    val totalCount: Int,
    val pricedCount: Int,
    val receivedCents: Long,
    val pendingCents: Long,
    val totalExpectedCents: Long,
    val averageTicketCents: Long
)

fun appointmentKey(year: Int, month: Int, day: Int, clientName: String): String {
    val norm = ClientNameHelper.normalize(clientName)
    return "${year}_${month}_${day}_$norm"
}

fun calculateMonthlyFinanceSummary(
    year: Int,
    month: Int,
    clients: List<Pair<Int, String>>, // (day, name)
    detailsMap: Map<String, AppointmentDetail>
): MonthlyFinanceSummary {
    val totalCount = clients.size
    var pricedCount = 0
    var receivedCents = 0L
    var pendingCents = 0L
    var totalExpectedCents = 0L

    for ((day, name) in clients) {
        val key = appointmentKey(year, month, day, name)
        val entry = detailsMap[key]
        if (entry != null && entry.priceCents > 0L) {
            pricedCount++
            totalExpectedCents += entry.priceCents
            receivedCents += entry.receivedCents
            pendingCents += entry.pendingCents
        }
    }

    val averageTicketCents = if (pricedCount > 0) totalExpectedCents / pricedCount else 0L
    return MonthlyFinanceSummary(
        totalCount = totalCount,
        pricedCount = pricedCount,
        receivedCents = receivedCents,
        pendingCents = pendingCents,
        totalExpectedCents = totalExpectedCents,
        averageTicketCents = averageTicketCents
    )
}

fun calculateMonthlyFinanceSummary(
    clients: List<Triple<Int, Int, String>>, // (day, slotIndex, name)
    detailsMap: Map<String, AppointmentDetail>
): MonthlyFinanceSummary {
    val totalCount = clients.size
    var pricedCount = 0
    var receivedCents = 0L
    var pendingCents = 0L
    var totalExpectedCents = 0L

    for ((day, _, name) in clients) {
        val norm = ClientNameHelper.normalize(name)
        val entry = detailsMap.entries.firstOrNull {
            it.key.endsWith("_${day}_$norm")
        }?.value

        if (entry != null && entry.priceCents > 0L) {
            pricedCount++
            totalExpectedCents += entry.priceCents
            receivedCents += entry.receivedCents
            pendingCents += entry.pendingCents
        }
    }

    val averageTicketCents = if (pricedCount > 0) totalExpectedCents / pricedCount else 0L
    return MonthlyFinanceSummary(
        totalCount = totalCount,
        pricedCount = pricedCount,
        receivedCents = receivedCents,
        pendingCents = pendingCents,
        totalExpectedCents = totalExpectedCents,
        averageTicketCents = averageTicketCents
    )
}
