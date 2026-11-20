package com.salu.calendario

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

enum class WhatsAppTemplateType(val label: String) {
    CONFIRMACAO("Confirmar"),
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
        template: WhatsAppTemplateType
    ): String {
        val formattedDate = formatClientDate(day, month, year)
        val cleanName = clientName.trim()

        return when (template) {
            WhatsAppTemplateType.CONFIRMACAO -> {
                "Olá, $cleanName! Tudo bem? ✨\n\n" +
                    "Passando para confirmar o seu agendamento no dia $formattedDate.\n" +
                    "Poderia me confirmar se o horário segue certinho para você?\n\n" +
                    "Qualquer dúvida ou ajuste, me avise por aqui! 💖"
            }
            WhatsAppTemplateType.LEMBRETE -> {
                "Oi, $cleanName! Tudo certinho? 🌸\n\n" +
                    "Passando para lembrar do seu atendimento marcado para $formattedDate.\n" +
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
