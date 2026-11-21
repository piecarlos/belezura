package com.salu.calendario

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

enum class WhatsAppTemplateType(val label: String) {
    CONFIRMACAO("Confirmar"),
    RESUMO_PIX("Resumo + Pix"),
    LEMBRETE("Lembrete"),
    POS_ATENDIMENTO("Pós-atendimento")
}

object WhatsAppHelper {

    fun formatClientDate(day: Int, month: Int, year: Int): String {
        val date = try {
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            LocalDate.now()
        }
        val dayOfWeekPt = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "segunda-feira"
            DayOfWeek.TUESDAY -> "terça-feira"
            DayOfWeek.WEDNESDAY -> "quarta-feira"
            DayOfWeek.THURSDAY -> "quinta-feira"
            DayOfWeek.FRIDAY -> "sexta-feira"
            DayOfWeek.SATURDAY -> "sábado"
            DayOfWeek.SUNDAY -> "domingo"
            else -> ""
        }
        return String.format(Locale("pt", "BR"), "%02d/%02d (%s)", day, month, dayOfWeekPt)
    }

    fun buildClientMessage(
        clientName: String,
        day: Int,
        month: Int,
        year: Int,
        template: WhatsAppTemplateType,
        detail: AppointmentDetail? = null,
        financeConfig: StudioFinanceConfig? = null
    ): String {
        val formattedDate = formatClientDate(day, month, year)
        val cleanName = clientName.trim()
        val timeStr = if (!detail?.time.isNullOrBlank()) " às ${detail?.time}" else ""
        val serviceStr = if (!detail?.service.isNullOrBlank()) " para ${detail?.service}" else ""

        return when (template) {
            WhatsAppTemplateType.CONFIRMACAO -> {
                val detailLine = if (timeStr.isNotEmpty() || serviceStr.isNotEmpty()) {
                    "Passando para confirmar o seu agendamento no dia $formattedDate$timeStr$serviceStr.\n"
                } else {
                    "Passando para confirmar o seu agendamento no dia $formattedDate.\n"
                }
                "Olá, $cleanName! Tudo bem? ✨\n\n" +
                    detailLine +
                    "Poderia me confirmar se o horário segue certinho para você?\n\n" +
                    "Qualquer dúvida ou ajuste, me avise por aqui! 💖"
            }
            WhatsAppTemplateType.RESUMO_PIX -> {
                val time = if (!detail?.time.isNullOrBlank()) detail?.time else "A combinar"
                val service = if (!detail?.service.isNullOrBlank()) detail?.service else "Atendimento"
                val price = if (detail != null && detail.priceCents > 0L) formatBrl(detail.priceCents) else "A combinar"
                val deposit = if (detail != null && detail.depositCents > 0L) formatBrl(detail.depositCents) else null
                val remaining = if (detail != null && detail.priceCents > 0L) formatBrl(detail.remainingCents) else null
                val paymentMethod = detail?.paymentMethod?.label ?: "Pix"

                val sb = StringBuilder()
                sb.append("✨ *Resumo do seu Atendimento* ✨\n")
                sb.append("Olá, *$cleanName*! Aqui estão os detalhes:\n\n")
                sb.append("📅 *Data:* $formattedDate\n")
                sb.append("⏰ *Horário:* $time\n")
                sb.append("💅 *Procedimento:* $service\n")
                sb.append("💰 *Valor:* $price\n")
                if (deposit != null) {
                    sb.append("💳 *Sinal:* $deposit\n")
                }
                if (remaining != null && detail != null && detail.depositCents > 0L) {
                    sb.append("💵 *Restante:* $remaining no dia ($paymentMethod)\n")
                }

                if (!financeConfig?.pixKey.isNullOrBlank()) {
                    sb.append("\n🔑 *Chave Pix para confirmação:*\n")
                    sb.append("`${financeConfig?.pixKey}`")
                    if (!financeConfig?.pixHolderName.isNullOrBlank()) {
                        sb.append(" (${financeConfig.pixHolderName})")
                    }
                    sb.append("\n")
                }

                sb.append("\nQualquer dúvida estou à disposição! 💖")
                sb.toString()
            }
            WhatsAppTemplateType.LEMBRETE -> {
                val detailLine = if (timeStr.isNotEmpty() || serviceStr.isNotEmpty()) {
                    "Passando para lembrar do seu atendimento marcado para $formattedDate$timeStr$serviceStr.\n"
                } else {
                    "Passando para lembrar do seu atendimento marcado para $formattedDate.\n"
                }
                "Oi, $cleanName! Tudo certinho? 🌸\n\n" +
                    detailLine +
                    "Já estou preparando tudo com muito carinho para receber você!\n\n" +
                    "Até logo! ✨"
            }
            WhatsAppTemplateType.POS_ATENDIMENTO -> {
                "Olá, $cleanName! Como você está? ✨\n\n" +
                    "Passando para agradecer pela confiança e carinho no atendimento do dia $formattedDate!\n" +
                    "Espero que tenha amado o resultado. Se precisar de qualquer cuidado ou retoque, estou à disposição! 💖"
            }
        }
    }

    fun openWhatsApp(context: Context, message: String, phone: String? = null): Boolean {
        val cleanDigits = phone?.filter { it.isDigit() } ?: ""
        val fullPhone = when {
            cleanDigits.length in 10..11 -> "55$cleanDigits"
            cleanDigits.length in 12..13 && cleanDigits.startsWith("55") -> cleanDigits
            else -> null
        }
        if (fullPhone != null) {
            val url = "https://wa.me/$fullPhone?text=" + java.net.URLEncoder.encode(message, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.setPackage("com.whatsapp")
            try {
                context.startActivity(intent)
                return true
            } catch (_: ActivityNotFoundException) {
                intent.setPackage("com.whatsapp.w4b")
                try {
                    context.startActivity(intent)
                    return true
                } catch (_: ActivityNotFoundException) {
                    intent.setPackage(null)
                    try {
                        context.startActivity(intent)
                        return true
                    } catch (_: Exception) {}
                }
            }
        }
        return openWhatsAppWithText(context, message)
    }

    fun copyToClipboard(context: Context, text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Mensagem Cliente", text)
            clipboard?.setPrimaryClip(clip)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openWhatsAppWithText(context: Context, message: String): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        if (context !is Activity) {
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 1. Tenta WhatsApp oficial
        sendIntent.setPackage("com.whatsapp")
        try {
            context.startActivity(sendIntent)
            return true
        } catch (_: ActivityNotFoundException) {
            // 2. Tenta WhatsApp Business (w4b)
            sendIntent.setPackage("com.whatsapp.w4b")
            try {
                context.startActivity(sendIntent)
                return true
            } catch (_: ActivityNotFoundException) {
                // 3. Fallback seguro via Seletor do Sistema (sem travar pacote)
                sendIntent.setPackage(null)
                val chooser = Intent.createChooser(sendIntent, "Enviar mensagem para cliente")
                if (context !is Activity) {
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return try {
                    context.startActivity(chooser)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
