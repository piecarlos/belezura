package com.salu.calendario

import org.json.JSONArray
import org.json.JSONObject

enum class PromoTemplateType(val displayName: String) {
    AGENDA_ABERTA("Vagas"),
    TABELA_PRECOS("Preços"),
    AVISO_REGRAS("Avisos")
}

data class PromoLineItem(
    val label: String,
    val value: String = ""
)

data class PromoDraft(
    val templateType: PromoTemplateType = PromoTemplateType.AGENDA_ABERTA,
    val badgeText: String = "",
    val title: String = "",
    val subtitle: String = "",
    val items: List<PromoLineItem> = emptyList(),
    val footerCta: String = "",
    val showLogo: Boolean = true,
    val titleFont: String = "PLAYFAIR_DISPLAY_BOLD",
    val accentColorHex: String = "",
    val logoScale: Float = 1.0f,
    val logoOffsetX: Float = 0f,
    val logoOffsetY: Float = 0f
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("templateType", templateType.name)
        root.put("badgeText", badgeText)
        root.put("title", title)
        root.put("subtitle", subtitle)
        val arr = JSONArray()
        items.take(MAX_ITEMS).forEach { item ->
            val obj = JSONObject()
            obj.put("label", item.label)
            obj.put("value", item.value)
            arr.put(obj)
        }
        root.put("items", arr)
        root.put("footerCta", footerCta)
        root.put("showLogo", showLogo)
        root.put("titleFont", titleFont)
        root.put("accentColorHex", accentColorHex)
        root.put("logoScale", logoScale.toDouble())
        root.put("logoOffsetX", logoOffsetX.toDouble())
        root.put("logoOffsetY", logoOffsetY.toDouble())
        return root.toString()
    }

    companion object {
        const val MAX_ITEMS = 6

        fun getPreset(type: PromoTemplateType): PromoDraft {
            return when (type) {
                PromoTemplateType.AGENDA_ABERTA -> PromoDraft(
                    templateType = PromoTemplateType.AGENDA_ABERTA,
                    badgeText = "ÚLTIMOS HORÁRIOS",
                    title = "Agenda Aberta",
                    subtitle = "Garanta seu momento de autocuidado esta semana ✨",
                    items = listOf(
                        PromoLineItem("Quinta-feira", "14:00 • 16:30"),
                        PromoLineItem("Sexta-feira", "10:00 • 15:00"),
                        PromoLineItem("Sábado", "09:30 • 11:00 • 14:00")
                    ),
                    footerCta = "Agende seu horário pelo WhatsApp!",
                    showLogo = true
                )
                PromoTemplateType.TABELA_PRECOS -> PromoDraft(
                    templateType = PromoTemplateType.TABELA_PRECOS,
                    badgeText = "COMBOS ESPECIAIS",
                    title = "Menu de Serviços",
                    subtitle = "Valores promocionais válidos por tempo limitado",
                    items = listOf(
                        PromoLineItem("Manicure + Pedicure", "R$ 75,00"),
                        PromoLineItem("Alongamento em Gel", "R$ 140,00"),
                        PromoLineItem("Manutenção Gel", "R$ 95,00"),
                        PromoLineItem("Spa dos Pés Completo", "R$ 60,00"),
                        PromoLineItem("Design de Sobrancelhas", "R$ 45,00")
                    ),
                    footerCta = "Reserve seu horário no direct ou WhatsApp",
                    showLogo = true
                )
                PromoTemplateType.AVISO_REGRAS -> PromoDraft(
                    templateType = PromoTemplateType.AVISO_REGRAS,
                    badgeText = "COMUNICADO IMPORTANTE",
                    title = "Avisos do Estúdio",
                    subtitle = "Para melhor atendê-la com conforto e pontualidade",
                    items = listOf(
                        PromoLineItem("Tolerância de atraso", "Máximo de 15 minutos"),
                        PromoLineItem("Cancelamentos", "Avisar com 24h de antecedência"),
                        PromoLineItem("Agendamentos", "Confirmação mediante sinal de 30%"),
                        PromoLineItem("Acompanhantes", "Espaço reservado apenas para clientes")
                    ),
                    footerCta = "Agradecemos o carinho e a compreensão! ❤️",
                    showLogo = true
                )
            }
        }

        fun fromJson(jsonStr: String): PromoDraft {
            if (jsonStr.isBlank()) {
                return getPreset(PromoTemplateType.AGENDA_ABERTA)
            }
            return try {
                val root = JSONObject(jsonStr)
                val typeStr = root.optString("templateType", PromoTemplateType.AGENDA_ABERTA.name)
                val templateType = try {
                    PromoTemplateType.valueOf(typeStr)
                } catch (_: Exception) {
                    PromoTemplateType.AGENDA_ABERTA
                }
                val fallback = getPreset(templateType)

                val badgeText = root.optString("badgeText", fallback.badgeText)
                val title = root.optString("title", fallback.title)
                val subtitle = root.optString("subtitle", fallback.subtitle)
                val footerCta = root.optString("footerCta", fallback.footerCta)
                val showLogo = root.optBoolean("showLogo", fallback.showLogo)
                val titleFont = root.optString("titleFont", fallback.titleFont)
                val accentColorHex = root.optString("accentColorHex", fallback.accentColorHex)
                val logoScale = root.optDouble("logoScale", fallback.logoScale.toDouble()).toFloat()
                val logoOffsetX = root.optDouble("logoOffsetX", fallback.logoOffsetX.toDouble()).toFloat()
                val logoOffsetY = root.optDouble("logoOffsetY", fallback.logoOffsetY.toDouble()).toFloat()

                val items = mutableListOf<PromoLineItem>()
                val arr = root.optJSONArray("items")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val itemObj = arr.optJSONObject(i) ?: continue
                        val label = itemObj.optString("label", "")
                        val value = itemObj.optString("value", "")
                        if (label.isNotBlank() || value.isNotBlank()) {
                            items.add(PromoLineItem(label, value))
                        }
                    }
                }

                val finalItems = if (items.isNotEmpty()) items.take(MAX_ITEMS) else fallback.items

                PromoDraft(
                    templateType = templateType,
                    badgeText = badgeText,
                    title = title,
                    subtitle = subtitle,
                    items = finalItems,
                    footerCta = footerCta,
                    showLogo = showLogo,
                    titleFont = titleFont,
                    accentColorHex = accentColorHex,
                    logoScale = logoScale,
                    logoOffsetX = logoOffsetX,
                    logoOffsetY = logoOffsetY
                )
            } catch (_: Exception) {
                getPreset(PromoTemplateType.AGENDA_ABERTA)
            }
        }
    }
}
