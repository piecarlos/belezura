package com.salu.calendario

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class SeasonalCampaign(
    val id: String,
    val title: String,
    val subtitle: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val themeTag: String,
    val daysRemaining: Long,
    val promoPreset: PromoDraft? = null
)

object SeasonalRadar {

    fun getPromoPresetForCampaign(id: String): PromoDraft {
        return when (id) {
            "summer" -> PromoDraft(
                templateType = PromoTemplateType.AGENDA_ABERTA,
                badgeText = "VERÃO & FÉRIAS",
                title = "Unhas & Pés Perfeitos",
                subtitle = "Horários especiais para arrasar na praia ou piscina ☀️",
                items = listOf(
                    PromoLineItem("Quinta-feira", "10:00 • 15:00"),
                    PromoLineItem("Sexta-feira", "09:00 • 14:00 • 17:30"),
                    PromoLineItem("Sábado", "08:30 • 11:30")
                ),
                footerCta = "Garanta sua vaga antes que esgote!",
                showLogo = true
            )
            "carnival" -> PromoDraft(
                templateType = PromoTemplateType.TABELA_PRECOS,
                badgeText = "GLITTER & FOLIA",
                title = "Especial Carnaval",
                subtitle = "Combos para brilhar nos blocos e festas 🎉",
                items = listOf(
                    PromoLineItem("Manicure com Glitter", "R$ 65,00"),
                    PromoLineItem("Alongamento Gel Folia", "R$ 130,00"),
                    PromoLineItem("Combo Make + Unhas", "R$ 160,00"),
                    PromoLineItem("Design Sobrancelhas", "R$ 45,00")
                ),
                footerCta = "Agende seu horário e caia na folia!",
                showLogo = true
            )
            "womens_day" -> PromoDraft(
                templateType = PromoTemplateType.TABELA_PRECOS,
                badgeText = "MÊS DA MULHER",
                title = "Celebre Sua Beleza",
                subtitle = "Preparamos mimos e condições especiais para você 🌸",
                items = listOf(
                    PromoLineItem("Mão + Pé + Spa", "R$ 85,00"),
                    PromoLineItem("Manutenção Gel", "R$ 90,00"),
                    PromoLineItem("Limpeza de Pele Express", "R$ 75,00"),
                    PromoLineItem("Design + Henna", "R$ 55,00")
                ),
                footerCta = "Você merece esse cuidado! Agende já",
                showLogo = true
            )
            "mothers_day" -> PromoDraft(
                templateType = PromoTemplateType.TABELA_PRECOS,
                badgeText = "ESPECIAL DIA DAS MÃES",
                title = "Presenteie com Amor",
                subtitle = "Combos para homenagear quem sempre cuida de você ❤️",
                items = listOf(
                    PromoLineItem("Combo Mãe & Filha", "R$ 140,00"),
                    PromoLineItem("Spa dos Pés + Manicure", "R$ 80,00"),
                    PromoLineItem("Alongamento Completo", "R$ 135,00"),
                    PromoLineItem("Vale-Presente Beleza", "A partir de R$ 50")
                ),
                footerCta = "Vagas limitadas para a semana das mães!",
                showLogo = true
            )
            "valentines_day" -> PromoDraft(
                templateType = PromoTemplateType.AGENDA_ABERTA,
                badgeText = "ESPECIAL NAMORADOS",
                title = "Produção Romântica",
                subtitle = "Horários para você se produzir para o date especial 🌹",
                items = listOf(
                    PromoLineItem("Quinta-feira", "14:00 • 17:00"),
                    PromoLineItem("Sexta-feira", "10:00 • 13:30 • 16:30"),
                    PromoLineItem("Sábado", "09:00 • 12:00")
                ),
                footerCta = "Reserve logo seu horário de date!",
                showLogo = true
            )
            "sao_joao" -> PromoDraft(
                templateType = PromoTemplateType.AGENDA_ABERTA,
                badgeText = "CLIMA JUNINO",
                title = "Unhas pro Arraiá",
                subtitle = "Fique pronta para curtir as festas de São João 🔥",
                items = listOf(
                    PromoLineItem("Quarta-feira", "11:00 • 15:30"),
                    PromoLineItem("Quinta-feira", "09:30 • 14:00"),
                    PromoLineItem("Sexta-feira", "10:00 • 16:00")
                ),
                footerCta = "Anarriê! Garanta sua vaga pelo WhatsApp",
                showLogo = true
            )
            "client_day" -> PromoDraft(
                templateType = PromoTemplateType.TABELA_PRECOS,
                badgeText = "SEMANA DA CLIENTE",
                title = "Obrigada Por Estar Aqui",
                subtitle = "Descontos exclusivos em todos os procedimentos 💖",
                items = listOf(
                    PromoLineItem("Manicure Tradicional", "R$ 35,00"),
                    PromoLineItem("Banho de Gel", "R$ 70,00"),
                    PromoLineItem("Spa dos Pés Relax", "R$ 50,00"),
                    PromoLineItem("Lash Lifting", "R$ 80,00")
                ),
                footerCta = "Válido apenas esta semana! Reserve já",
                showLogo = true
            )
            "pink_october" -> PromoDraft(
                templateType = PromoTemplateType.AVISO_REGRAS,
                badgeText = "OUTUBRO ROSA",
                title = "Cuidado & Amor Próprio",
                subtitle = "O autocuidado começa de dentro para fora 🎀",
                items = listOf(
                    PromoLineItem("Faça o autoexame", "O diagnóstico precoce salva vidas"),
                    PromoLineItem("Seu momento de pausa", "Reserve um tempo exclusivo só para você"),
                    PromoLineItem("Tons de rosa em alta", "Esmaltação temática especial no estúdio"),
                    PromoLineItem("Compartilhe amor", "Incentive as mulheres da sua vida a se cuidarem")
                ),
                footerCta = "Juntas na prevenção e no amor próprio!",
                showLogo = true
            )
            "black_friday" -> PromoDraft(
                templateType = PromoTemplateType.TABELA_PRECOS,
                badgeText = "BLACK WEEK DA BELEZA",
                title = "Super Combos Black",
                subtitle = "Os menores preços do ano em horários selecionados 🖤",
                items = listOf(
                    PromoLineItem("Alongamento Gel + Pé", "R$ 130,00"),
                    PromoLineItem("Manutenção + Cutilagem", "R$ 85,00"),
                    PromoLineItem("Spa dos Pés Completo", "R$ 55,00"),
                    PromoLineItem("Combo Sobrancelha + Buço", "R$ 40,00")
                ),
                footerCta = "Últimas vagas com desconto! Agende já",
                showLogo = true
            )
            "christmas_newyear" -> PromoDraft(
                templateType = PromoTemplateType.AGENDA_ABERTA,
                badgeText = "FESTAS DE FIM DE ANO",
                title = "Agenda de Natal & Réveillon",
                subtitle = "Horários concorridos para fechar o ano impecável ✨",
                items = listOf(
                    PromoLineItem("Semana do Natal", "21/12 a 24/12 (vagas finais)"),
                    PromoLineItem("Semana do Réveillon", "28/12 a 31/12 (esgotando)"),
                    PromoLineItem("Horários Extras", "Consulte encaixes no direct ou WhatsApp")
                ),
                footerCta = "Garanta seu visual de festas! Agende agora",
                showLogo = true
            )
            else -> PromoDraft.getPreset(PromoTemplateType.AGENDA_ABERTA)
        }
    }

    // Carnaval móvel (2026 a 2030)
    private val CARNIVAL_DATES = mapOf(
        2026 to LocalDate.of(2026, 2, 17),
        2027 to LocalDate.of(2027, 2, 9),
        2028 to LocalDate.of(2028, 2, 29),
        2029 to LocalDate.of(2029, 2, 13),
        2030 to LocalDate.of(2030, 3, 5)
    )

    fun getActiveCampaign(today: LocalDate = LocalDate.now()): SeasonalCampaign? {
        val campaigns = getAllUpcomingCampaigns(today)
        return campaigns.minByOrNull { it.daysRemaining }
    }

    fun getAllUpcomingCampaigns(today: LocalDate = LocalDate.now()): List<SeasonalCampaign> {
        val year = today.year
        val list = mutableListOf<SeasonalCampaign>()

        // 1. Verão / Férias (01/01 a 31/01)
        val summerStart = LocalDate.of(year, 1, 1)
        val summerEnd = LocalDate.of(year, 1, 31)
        evalPeriod(today, "summer", "Verão & Férias", "Unhas e cuidados de verão em alta", summerStart, summerEnd, "summer")?.let { list.add(it) }

        // 2. Carnaval (período ativo de 10 dias antes até o dia)
        CARNIVAL_DATES[year]?.let { carnivalDate ->
            val carnivalStart = carnivalDate.minusDays(10)
            evalPeriod(today, "carnival", "Carnaval", "Glitter e unhas festivas em alta", carnivalStart, carnivalDate, "carnival")?.let { list.add(it) }
        }

        // 3. Dia da Mulher (08/03)
        val womensDay = LocalDate.of(year, 3, 8)
        evalSingleDay(today, "womens_day", "Dia da Mulher", "Celebre a beleza feminina", womensDay, "womens_day")?.let { list.add(it) }

        // 4. Dia das Mães (2º domingo de maio)
        val mothersDay = LocalDate.of(year, 5, 1).with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SUNDAY))
        evalSingleDay(today, "mothers_day", "Dia das Mães", "Presentes e cuidados para mães", mothersDay, "mothers_day")?.let { list.add(it) }

        // 5. Dia dos Namorados (12/06)
        val valentines = LocalDate.of(year, 6, 12)
        evalSingleDay(today, "valentines_day", "Dia dos Namorados", "Produções românticas para a data", valentines, "valentines_day")?.let { list.add(it) }

        // 6. São João (24/06)
        val saoJoao = LocalDate.of(year, 6, 24)
        evalSingleDay(today, "sao_joao", "São João", "Temas festivos e cores alegres", saoJoao, "sao_joao")?.let { list.add(it) }

        // 7. Dia da Cliente (15/09)
        val clientDay = LocalDate.of(year, 9, 15)
        evalSingleDay(today, "client_day", "Dia da Cliente", "Valorize e fidelize suas clientes", clientDay, "client_day")?.let { list.add(it) }

        // 8. Outubro Rosa (01/10 a 31/10)
        val pinkStart = LocalDate.of(year, 10, 1)
        val pinkEnd = LocalDate.of(year, 10, 31)
        evalPeriod(today, "pink_october", "Outubro Rosa", "Conscientização e tons de rosa", pinkStart, pinkEnd, "pink_october")?.let { list.add(it) }

        // 9. Black Friday (última sexta-feira de novembro)
        val blackFriday = LocalDate.of(year, 11, 1).with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY))
        evalSingleDay(today, "black_friday", "Black Friday", "Combos promocionais e vagas da semana", blackFriday, "black_friday")?.let { list.add(it) }

        // 10. Festas de Fim de Ano (Natal e Réveillon: 15/12 a 31/12)
        val holidaysStart = LocalDate.of(year, 12, 15)
        val holidaysEnd = LocalDate.of(year, 12, 31)
        evalPeriod(today, "christmas_newyear", "Festas de Fim de Ano", "Natal e Réveillon: alta temporada", holidaysStart, holidaysEnd, "christmas_newyear")?.let { list.add(it) }

        return list.sortedBy { it.daysRemaining }
    }

    private fun evalSingleDay(
        today: LocalDate,
        id: String,
        name: String,
        subtitle: String,
        target: LocalDate,
        themeTag: String
    ): SeasonalCampaign? {
        val days = ChronoUnit.DAYS.between(today, target)
        val preset = getPromoPresetForCampaign(id)
        return when {
            days == 0L -> {
                SeasonalCampaign(
                    id = id,
                    title = "Hoje é $name!",
                    subtitle = subtitle,
                    startDate = target,
                    endDate = target,
                    themeTag = themeTag,
                    daysRemaining = 0,
                    promoPreset = preset
                )
            }
            days in 1..20 -> {
                val title = if (days <= 5) "$name chegando ($days dias)" else "Em alta: $name"
                SeasonalCampaign(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    startDate = target.minusDays(days),
                    endDate = target,
                    themeTag = themeTag,
                    daysRemaining = days,
                    promoPreset = preset
                )
            }
            else -> null
        }
    }

    private fun evalPeriod(
        today: LocalDate,
        id: String,
        name: String,
        subtitle: String,
        startDate: LocalDate,
        endDate: LocalDate,
        themeTag: String
    ): SeasonalCampaign? {
        val preset = getPromoPresetForCampaign(id)
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            return SeasonalCampaign(
                id = id,
                title = "Em alta agora: $name",
                subtitle = subtitle,
                startDate = startDate,
                endDate = endDate,
                themeTag = themeTag,
                daysRemaining = 0,
                promoPreset = preset
            )
        }

        val daysUntilStart = ChronoUnit.DAYS.between(today, startDate)
        if (daysUntilStart in 1..20) {
            val title = if (daysUntilStart <= 5) "$name chegando ($daysUntilStart dias)" else "Em alta: $name"
            return SeasonalCampaign(
                id = id,
                title = title,
                subtitle = subtitle,
                startDate = startDate,
                endDate = endDate,
                themeTag = themeTag,
                daysRemaining = daysUntilStart,
                promoPreset = preset
            )
        }

        return null
    }
}
