package com.salu.calendario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader

object ThemeCatalog : ThemeRepository {

    private val themeList = listOf(
        // Temas Gratuitos (100% funcionais imediatamente)
        ThemeItem(
            id = "floral_default",
            name = "Floral Clássico",
            category = ThemeCategory.FLORAL,
            isPro = false,
            sourceType = ThemeSourceType.BUILTIN,
            accentHex = "#E64886",
            drawableResId = R.drawable.bg_base,
            description = "Tema oficial com flores de cerejeira em degradê suave.",
            palette = ThemePalette(
                monthColorHex = "#5E2326",
                yearColorHex = "#E64886",
                calendarAccentHex = "#E89BAE",
                backgroundBaseHex = "#FFF5F8",
                suggestedMonthFont = HeaderFontOption.CINZEL_BOLD,
                suggestedYearFont = HeaderFontOption.FREDOKA_BOLD,
                defaultCalendarScale = 1.0f
            )
        ),
        ThemeItem(
            id = "make_glam_luxo",
            name = "Make Glam Luxo",
            category = ThemeCategory.FLORAL,
            isPro = false,
            sourceType = ThemeSourceType.BUILTIN,
            accentHex = "#B89047",
            drawableResId = R.drawable.bg_make_glam,
            description = "Moldura elegante com maquiagem e detalhes em ouro e rosé.",
            palette = ThemePalette(
                monthColorHex = "#5C2E3E",
                yearColorHex = "#B89047",
                calendarAccentHex = "#E2B4BD",
                backgroundBaseHex = "#FFF5F8",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.88f
            )
        ),
        ThemeItem(
            id = "make_glam_floral",
            name = "Make Glam Floral",
            category = ThemeCategory.FLORAL,
            isPro = false,
            sourceType = ThemeSourceType.BUILTIN,
            accentHex = "#C98A95",
            drawableResId = R.drawable.bg_make_glam_2,
            description = "Composição glamourosa com flores suaves e pincéis de maquiagem.",
            palette = ThemePalette(
                monthColorHex = "#5C2E3E",
                yearColorHex = "#C98A95",
                calendarAccentHex = "#E2B4BD",
                backgroundBaseHex = "#FFF5F8",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.88f
            )
        ),
        ThemeItem(
            id = "minimal_cream",
            name = "Minimalista Clean",
            category = ThemeCategory.MINIMALIST,
            isPro = false,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#C4A882",
            description = "Fundo creme aconchegante com moldura geométrica fina.",
            palette = ThemePalette(
                monthColorHex = "#4A3F35",
                yearColorHex = "#8C735B",
                calendarAccentHex = "#C8B6A6",
                backgroundBaseHex = "#FAF6F2",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "rose_editorial",
            name = "Rosé Editorial",
            category = ThemeCategory.FLORAL,
            isPro = false,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#E56B6F",
            description = "Degradê rosé e champagne com detalhes delicados.",
            palette = ThemePalette(
                monthColorHex = "#6B3A46",
                yearColorHex = "#C97B84",
                calendarAccentHex = "#E2B4BD",
                backgroundBaseHex = "#FFF2F5",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "luxury_gold",
            name = "Luxo Off-White & Ouro",
            category = ThemeCategory.LUXURY,
            isPro = false,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#C9972B",
            description = "Fundo claro nobre com moldura dupla em tom dourado.",
            palette = ThemePalette(
                monthColorHex = "#3E3224",
                yearColorHex = "#B89047",
                calendarAccentHex = "#C9A86A",
                backgroundBaseHex = "#FDFBF7",
                suggestedMonthFont = HeaderFontOption.CINZEL_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),

        // 6 Temas de Prévia PRO (com selo de diamante transparente)
        ThemeItem(
            id = "black_friday_glam",
            name = "Black Friday Glam",
            category = ThemeCategory.SEASONAL,
            seasonalTag = "black_friday",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#D4AF37",
            description = "Estética premium para a maior data promocional do ano.",
            palette = ThemePalette(
                monthColorHex = "#D4AF37",
                yearColorHex = "#F3E5AB",
                calendarAccentHex = "#D4AF37",
                backgroundBaseHex = "#1C1A1C",
                suggestedMonthFont = HeaderFontOption.CINZEL_BOLD,
                suggestedYearFont = HeaderFontOption.FREDOKA_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "mothers_day_floral",
            name = "Dia das Mães Floral",
            category = ThemeCategory.SEASONAL,
            seasonalTag = "mothers_day",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#E64886",
            description = "Tons suaves de rosa e flores delicadas para homenagens.",
            palette = ThemePalette(
                monthColorHex = "#6B2B44",
                yearColorHex = "#E64886",
                calendarAccentHex = "#EAAEC0",
                backgroundBaseHex = "#FFF4F7",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.FREDOKA_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "valentines_romance",
            name = "Dia dos Namorados",
            category = ThemeCategory.COMMEMORATIVE,
            seasonalTag = "valentines_day",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#C62828",
            description = "Clima romântico e tons rubi para produções especiais.",
            palette = ThemePalette(
                monthColorHex = "#5C1518",
                yearColorHex = "#C62828",
                calendarAccentHex = "#E57373",
                backgroundBaseHex = "#FFF1F3",
                suggestedMonthFont = HeaderFontOption.PLAYFAIR_DISPLAY_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "pink_october",
            name = "Outubro Rosa",
            category = ThemeCategory.SEASONAL,
            seasonalTag = "pink_october",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#FF69B4",
            description = "Conscientização feminina com tons vibrantes de rosa.",
            palette = ThemePalette(
                monthColorHex = "#7A1C48",
                yearColorHex = "#FF4081",
                calendarAccentHex = "#F48FB1",
                backgroundBaseHex = "#FFF0F5",
                suggestedMonthFont = HeaderFontOption.CINZEL_BOLD,
                suggestedYearFont = HeaderFontOption.FREDOKA_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "christmas_gold",
            name = "Natal & Réveillon Dourado",
            category = ThemeCategory.SEASONAL,
            seasonalTag = "christmas_newyear",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#C9972B",
            description = "Brilho e sofisticação para fechar o ano com agenda cheia.",
            palette = ThemePalette(
                monthColorHex = "#521419",
                yearColorHex = "#C9972B",
                calendarAccentHex = "#E2BA62",
                backgroundBaseHex = "#FAF7EF",
                suggestedMonthFont = HeaderFontOption.CINZEL_BOLD,
                suggestedYearFont = HeaderFontOption.CINZEL_BOLD,
                defaultCalendarScale = 0.90f
            )
        ),
        ThemeItem(
            id = "summer_tropical",
            name = "Verão Tropical",
            category = ThemeCategory.SEASONAL,
            seasonalTag = "summer",
            isPro = true,
            sourceType = ThemeSourceType.PROCEDURAL,
            accentHex = "#2E6B53",
            description = "Luminosidade natural e frescor para a temporada de praia.",
            palette = ThemePalette(
                monthColorHex = "#164E3B",
                yearColorHex = "#2E6B53",
                calendarAccentHex = "#80CBC4",
                backgroundBaseHex = "#F8FBF8",
                suggestedMonthFont = HeaderFontOption.FREDOKA_BOLD,
                suggestedYearFont = HeaderFontOption.FREDOKA_BOLD,
                defaultCalendarScale = 0.90f
            )
        )
    )

    private val proceduralCache = mutableMapOf<String, Bitmap>()

    override fun getThemes(): List<ThemeItem> = themeList

    override fun getThemeById(id: String): ThemeItem? = themeList.find { it.id == id }

    override fun getThemesByCategory(category: ThemeCategory): List<ThemeItem> {
        return if (category == ThemeCategory.ALL) {
            themeList
        } else {
            themeList.filter { it.category == category }
        }
    }

    /**
     * Retorna o bitmap de alta resolução 1080x1920 para o tema indicado.
     */
    fun getThemeBitmap(context: Context, themeId: String): Bitmap {
        if (themeId == "floral_default") {
            return BackgroundManager.getBackgroundBitmap(context)
        }
        if (themeId == "custom") {
            return BackgroundManager.getBackgroundBitmap(context)
        }

        // Cache procedural / drawable
        proceduralCache[themeId]?.let {
            if (!it.isRecycled) return it
        }

        val theme = getThemeById(themeId)
        if (theme?.drawableResId != null) {
            val options = BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val loaded = try {
                BitmapFactory.decodeResource(context.resources, theme.drawableResId, options)
            } catch (e: Exception) {
                null
            }
            if (loaded != null) {
                proceduralCache[themeId] = loaded
                return loaded
            }
        }

        val generated = generateProceduralBitmap(themeId)
        proceduralCache[themeId] = generated
        return generated
    }

    private fun generateProceduralBitmap(themeId: String): Bitmap {
        val width = 1080
        val height = 1920
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (themeId) {
            "minimal_cream" -> {
                // Fundo creme suave
                canvas.drawColor(Color.parseColor("#FAF6F2"))

                // Moldura externa fina
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.parseColor("#D4B895")
                val outer = RectF(28f, 28f, 1052f, 1892f)
                canvas.drawRoundRect(outer, 18f, 18f, paint)

                // Moldura interna delicada
                paint.strokeWidth = 1.2f
                paint.color = Color.parseColor("#E6D6C4")
                val inner = RectF(44f, 44f, 1036f, 1876f)
                canvas.drawRoundRect(inner, 12f, 12f, paint)

                // Detalhes geométricos sutis nos 4 cantos
                drawCornerDiamonds(canvas, 28f, 28f, 1052f, 1892f, Color.parseColor("#C4A882"))
            }

            "rose_editorial" -> {
                // Degradê vertical suave de rosé para champagne
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#FFF2F5"),
                    Color.parseColor("#FDF7F0"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Moldura dupla rosé
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                paint.color = Color.parseColor("#E8A8BA")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 24f, 24f, paint)

                paint.strokeWidth = 1.2f
                paint.color = Color.parseColor("#E8D2C2")
                canvas.drawRoundRect(RectF(44f, 44f, 1036f, 1876f), 18f, 18f, paint)
            }

            "luxury_gold" -> {
                // Fundo off-white nobre
                canvas.drawColor(Color.parseColor("#FDFBF7"))

                // Moldura externa ouro suave
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                paint.color = Color.parseColor("#D4AF37")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 16f, 16f, paint)

                // Moldura interna fina
                paint.strokeWidth = 1.5f
                paint.color = Color.parseColor("#E8D69A")
                canvas.drawRoundRect(RectF(44f, 44f, 1036f, 1876f), 10f, 10f, paint)

                drawCornerDiamonds(canvas, 28f, 28f, 1052f, 1892f, Color.parseColor("#D4AF37"))
            }

            // Prévias procedurais dos temas PRO
            "black_friday_glam" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#1C1A1C"),
                    Color.parseColor("#2C2228"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.parseColor("#D4AF37")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 20f, 20f, paint)
            }

            "mothers_day_floral" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#FFF4F7"),
                    Color.parseColor("#FBE8EE"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                paint.color = Color.parseColor("#E64886")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 22f, 22f, paint)
            }

            "valentines_romance" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#FFF1F3"),
                    Color.parseColor("#FCE4E8"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                paint.color = Color.parseColor("#C62828")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 20f, 20f, paint)
            }

            "pink_october" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#FFF0F5"),
                    Color.parseColor("#FCE0EA"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                paint.color = Color.parseColor("#FF69B4")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 20f, 20f, paint)
            }

            "christmas_gold" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#FAF7EF"),
                    Color.parseColor("#F5EEDD"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.parseColor("#C9972B")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 20f, 20f, paint)
            }

            "summer_tropical" -> {
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#F8FBF8"),
                    Color.parseColor("#EBF3EE"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                paint.color = Color.parseColor("#2E6B53")
                canvas.drawRoundRect(RectF(28f, 28f, 1052f, 1892f), 20f, 20f, paint)
            }

            else -> {
                canvas.drawColor(Color.WHITE)
            }
        }

        return bmp
    }

    private fun drawCornerDiamonds(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color
        }
        val size = 8f
        val path = Path()

        val points = listOf(
            Pair(left, top),
            Pair(right, top),
            Pair(left, bottom),
            Pair(right, bottom)
        )

        for ((cx, cy) in points) {
            path.reset()
            path.moveTo(cx, cy - size)
            path.lineTo(cx + size, cy)
            path.lineTo(cx, cy + size)
            path.lineTo(cx - size, cy)
            path.close()
            canvas.drawPath(path, paint)
        }
    }
}
