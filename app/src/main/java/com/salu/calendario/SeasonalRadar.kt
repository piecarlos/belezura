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
    val daysRemaining: Long
)

object SeasonalRadar {

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
        return when {
            days == 0L -> {
                SeasonalCampaign(
                    id = id,
                    title = "Hoje é $name!",
                    subtitle = subtitle,
                    startDate = target,
                    endDate = target,
                    themeTag = themeTag,
                    daysRemaining = 0
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
                    daysRemaining = days
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
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            return SeasonalCampaign(
                id = id,
                title = "Em alta agora: $name",
                subtitle = subtitle,
                startDate = startDate,
                endDate = endDate,
                themeTag = themeTag,
                daysRemaining = 0
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
                daysRemaining = daysUntilStart
            )
        }

        return null
    }
}
