package com.salu.calendario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.graphics.ColorUtils
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min

object CalendarRenderer {
    const val VIRTUAL_WIDTH = 1080f
    const val VIRTUAL_HEIGHT = 1920f

    // Pre-allocated reusable Paint and Rect objects for high-performance rendering
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(16f, 10f), 0f)
    }

    private val srcRect = Rect()
    private val dstRect = RectF()
    private val cardRect = RectF()
    private val clipPath = Path()
    private val swooshPath = Path()

    fun drawCalendar(
        canvas: Canvas,
        context: Context,
        yearMonth: YearMonth,
        cells: List<CellData>,
        layoutConfig: LayoutConfig,
        isEditMode: Boolean = false,
        selectedElement: EditableElement = EditableElement.NONE,
        bgBitmapOverride: Bitmap? = null
    ) {
        // 1. Draw Background (from ThemeCatalog or override)
        val bgBitmap = bgBitmapOverride ?: ThemeCatalog.getThemeBitmap(context, layoutConfig.selectedThemeId)
        srcRect.set(0, 0, bgBitmap.width, bgBitmap.height)
        dstRect.set(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT)
        canvas.drawBitmap(bgBitmap, srcRect, dstRect, bgPaint)

        // 2. Draw Month Name
        val monthTypeface = FontManager.getTypeface(context, layoutConfig.monthFont)
        val monthText = CalendarEngine.getMonthDisplayName(yearMonth)
        val monthScale = layoutConfig.monthScale
        var monthTextSize = 64f * monthScale

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.isSubpixelText = true
        textPaint.typeface = monthTypeface
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = layoutConfig.monthColor
        textPaint.textSize = monthTextSize
        textPaint.setShadowLayer(5f * monthScale, 0f, 2.5f * monthScale, Color.parseColor("#40000000"))

        // Auto-shrink month text if it exceeds safe width between flowers
        val maxSafeMonthWidth = 560f * monthScale
        val measuredMonthWidth = textPaint.measureText(monthText)
        if (measuredMonthWidth > maxSafeMonthWidth && maxSafeMonthWidth > 0f) {
            monthTextSize *= (maxSafeMonthWidth / measuredMonthWidth)
            textPaint.textSize = monthTextSize
        }
        canvas.drawText(monthText, layoutConfig.monthX, layoutConfig.monthY, textPaint)
        textPaint.clearShadowLayer()

        // 3. Draw 3D Styled Year + Swoosh
        val yearTypeface = FontManager.getTypeface(context, layoutConfig.yearFont)
        val yearText = yearMonth.year.toString()
        val yearScale = layoutConfig.yearScale
        val yearTextSize = 132f * yearScale
        val yearX = layoutConfig.yearX
        val yearY = layoutConfig.yearY

        val yearPalette = computeYearPalette(layoutConfig.yearColor)
        draw3DYear(canvas, yearText, yearX, yearY, yearTextSize, yearTypeface, yearScale, yearPalette)
        drawYearSwoosh(canvas, yearX, yearY, yearTextSize, yearScale, yearPalette)

        // 4. Draw Calendar Card and Grid
        val rowCount = CalendarEngine.calculateRowCount(yearMonth)
        val cardBounds = calculateCardBounds(layoutConfig)
        drawCalendarCard(
            canvas = canvas,
            context = context,
            cardBounds = cardBounds,
            rowCount = rowCount,
            cells = cells,
            layoutConfig = layoutConfig
        )

        // 5. Draw Logo (if enabled and loaded)
        val logoBitmap = if (layoutConfig.logoEnabled) LogoManager.getLogoBitmap(context) else null
        if (layoutConfig.logoEnabled && logoBitmap != null) {
            drawLogo(canvas, logoBitmap, layoutConfig)
        }

        // 6. Draw Footer Message (if enabled and not blank)
        if (layoutConfig.showFooterMessage && layoutConfig.footerMessage.isNotBlank()) {
            drawFooterMessage(canvas, context, layoutConfig)
        }

        // 7. Draw Edit Mode outlines if enabled
        if (isEditMode) {
            drawEditModeHighlights(
                canvas = canvas,
                layoutConfig = layoutConfig,
                cardBounds = cardBounds,
                selectedElement = selectedElement,
                monthText = monthText,
                monthTextSize = monthTextSize,
                yearText = yearText,
                yearTextSize = yearTextSize,
                monthTypeface = monthTypeface,
                yearTypeface = yearTypeface,
                logoBitmap = logoBitmap,
                context = context
            )
        }
    }

    private data class YearColorPalette(
        val gradTop: Int,
        val gradBottom: Int,
        val extrude: Int,
        val shadow: Int,
        val swooshStart: Int,
        val swooshEnd: Int
    )

    private fun computeYearPalette(baseColor: Int): YearColorPalette {
        // Exact v1 fidelity when baseColor is the default #E64886
        if (baseColor == 0xFFE64886.toInt()) {
            return YearColorPalette(
                gradTop = Color.parseColor("#FF7FB3"),
                gradBottom = Color.parseColor("#D83874"),
                extrude = Color.parseColor("#B82B62"),
                shadow = Color.parseColor("#407A1C3C"),
                swooshStart = Color.parseColor("#D83874"),
                swooshEnd = Color.parseColor("#F471A2")
            )
        }

        // HSV-derived volume & 3D styling for any custom color
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        val h = hsv[0]
        val s = hsv[1]
        val v = hsv[2]

        // Top: +18% brightness, -12% saturation
        val topHsv = floatArrayOf(h, (s - 0.12f).coerceIn(0f, 1f), (v + 0.18f).coerceIn(0f, 1f))
        val gradTop = Color.HSVToColor(topHsv)
        val gradBottom = baseColor

        // Extrusion: -30% brightness
        val extrudeHsv = floatArrayOf(h, s, (v - 0.30f).coerceIn(0f, 1f))
        val extrude = Color.HSVToColor(extrudeHsv)

        // Shadow: ~25% alpha dark shade
        val shadowHsv = floatArrayOf(h, s, (v - 0.45f).coerceIn(0f, 1f))
        val shadow = Color.HSVToColor(0x40, shadowHsv)

        return YearColorPalette(
            gradTop = gradTop,
            gradBottom = gradBottom,
            extrude = extrude,
            shadow = shadow,
            swooshStart = gradBottom,
            swooshEnd = gradTop
        )
    }

    private fun computeWeekdayBandColor(accentColor: Int): Int {
        if (accentColor == 0xFFE89BAE.toInt()) {
            return Color.parseColor("#FCEBF1")
        }
        val hsv = FloatArray(3)
        Color.colorToHSV(accentColor, hsv)
        // Soft pastel tint derived from accent color
        val bandHsv = floatArrayOf(hsv[0], (hsv[1] * 0.20f).coerceIn(0.04f, 0.16f), 0.98f)
        return Color.HSVToColor(bandHsv)
    }

    private fun draw3DYear(
        canvas: Canvas,
        yearText: String,
        x: Float,
        y: Float,
        textSize: Float,
        typeface: Typeface,
        scale: Float,
        palette: YearColorPalette
    ) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.isSubpixelText = true
        textPaint.typeface = typeface
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize

        // Layer 1: Projected soft shadow (+3f, +8f)
        textPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint.strokeWidth = 5f * scale
        textPaint.strokeJoin = Paint.Join.ROUND
        textPaint.color = palette.shadow
        canvas.drawText(yearText, x + (3f * scale), y + (8f * scale), textPaint)

        // Layer 2: 3D Extrusion (0f, +7f)
        textPaint.color = palette.extrude
        textPaint.strokeWidth = 10f * scale
        canvas.drawText(yearText, x, y + (7f * scale), textPaint)

        // Layer 3: Main body with vertical LinearGradient
        val gradTop = y - textSize * 0.85f
        val gradBottom = y + 15f * scale
        val bodyShader = LinearGradient(
            x, gradTop,
            x, gradBottom,
            palette.gradTop,
            palette.gradBottom,
            Shader.TileMode.CLAMP
        )
        textPaint.shader = bodyShader
        textPaint.strokeWidth = 7f * scale
        canvas.drawText(yearText, x, y, textPaint)
        textPaint.shader = null

        // Layer 4: Subtle top highlight (0f, -2f) with translucent white (#45FFFFFF)
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = 2.5f * scale
        textPaint.color = Color.parseColor("#45FFFFFF")
        canvas.drawText(yearText, x, y - (2f * scale), textPaint)
    }

    private fun drawYearSwoosh(
        canvas: Canvas,
        yearX: Float,
        yearY: Float,
        yearTextSize: Float,
        scale: Float,
        palette: YearColorPalette
    ) {
        val swooshY = yearY + (26f * scale)
        val halfWidth = 175f * scale

        swooshPath.reset()
        swooshPath.moveTo(yearX - halfWidth, swooshY)
        swooshPath.quadTo(
            yearX, swooshY + (16f * scale),
            yearX + halfWidth, swooshY
        )

        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = 7f * scale
        strokePaint.shader = LinearGradient(
            yearX - halfWidth, swooshY,
            yearX + halfWidth, swooshY,
            palette.swooshStart,
            palette.swooshEnd,
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(swooshPath, strokePaint)
        strokePaint.shader = null
    }

    fun calculateCardBounds(layoutConfig: LayoutConfig): RectF {
        val baseL = layoutConfig.cardLeft
        val baseR = layoutConfig.cardRight
        val baseT = layoutConfig.cardTop
        val baseB = layoutConfig.cardBottom
        val scale = layoutConfig.cardScale
        val offX = layoutConfig.cardOffsetX
        val offY = layoutConfig.cardOffsetY

        val cx = (baseL + baseR) / 2f + offX
        val cy = (baseT + baseB) / 2f + offY
        val halfW = ((baseR - baseL) * scale) / 2f
        val halfH = ((baseB - baseT) * scale) / 2f

        return RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
    }

    private fun drawCalendarCard(
        canvas: Canvas,
        context: Context,
        cardBounds: RectF,
        rowCount: Int,
        cells: List<CellData>,
        layoutConfig: LayoutConfig
    ) {
        val cornerRadius = 38f * layoutConfig.cardScale
        val cLeft = cardBounds.left
        val cRight = cardBounds.right
        val cTop = cardBounds.top
        val cBottom = cardBounds.bottom

        val accentColor = layoutConfig.cardAccentColor
        val isBlackFriday = layoutConfig.selectedThemeId == "black_friday_luxo" || layoutConfig.selectedThemeId == "black_friday_glam"

        val bandBgColor = if (isBlackFriday) {
            Color.parseColor("#1E1B18")
        } else {
            computeWeekdayBandColor(accentColor)
        }
        val textMonthColor = layoutConfig.monthColor
        val weekdayTextColor = if (isBlackFriday) {
            Color.parseColor("#F5D77A")
        } else {
            textMonthColor
        }
        val dayNumColor = if (isBlackFriday) {
            Color.parseColor("#7A5A12")
        } else {
            textMonthColor
        }

        // 1. Shadow around the card
        fillPaint.reset()
        fillPaint.isAntiAlias = true
        fillPaint.color = Color.parseColor("#15000000")
        cardRect.set(cLeft + 2f, cTop + 4f, cRight + 2f, cBottom + 6f)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, fillPaint)

        // 2. Card background (#FAFFFFFF)
        fillPaint.color = Color.parseColor("#FAFFFFFF")
        cardRect.set(cLeft, cTop, cRight, cBottom)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, fillPaint)

        // 3. Weekday header band (clipped to top rounded corners)
        val headerHeight = 96f * layoutConfig.cardScale
        val colWidth = (cRight - cLeft) / 7f

        canvas.save()
        clipPath.reset()
        clipPath.addRoundRect(cardRect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        // Weekday band background
        fillPaint.color = bandBgColor
        canvas.drawRect(cLeft, cTop, cRight, cTop + headerHeight, fillPaint)

        // Weekday letters (D S T Q Q S S) using Month Font and Month Color
        val headerTypeface = FontManager.getTypeface(context, layoutConfig.monthFont)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.isSubpixelText = true
        textPaint.typeface = headerTypeface
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = weekdayTextColor
        textPaint.textSize = 46f * layoutConfig.cardScale

        val fontMetrics = textPaint.fontMetrics
        val headerTextBaseline = cTop + (headerHeight / 2f) - ((fontMetrics.ascent + fontMetrics.descent) / 2f)

        for (col in 0 until 7) {
            val letter = CalendarEngine.WEEKDAY_HEADERS[col]
            val letterX = cLeft + (col * colWidth) + (colWidth / 2f)
            canvas.drawText(letter, letterX, headerTextBaseline, textPaint)
        }

        // Horizontal line under weekday header band
        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = accentColor
        strokePaint.strokeWidth = 2.5f * layoutConfig.cardScale
        canvas.drawLine(cLeft, cTop + headerHeight, cRight, cTop + headerHeight, strokePaint)

        // 4. Grid lines
        val gridHeight = cBottom - (cTop + headerHeight)
        val rowHeight = gridHeight / rowCount.toFloat()

        // Vertical lines
        for (col in 1 until 7) {
            val x = cLeft + col * colWidth
            canvas.drawLine(x, cTop + headerHeight, x, cBottom, strokePaint)
        }

        // Horizontal lines
        for (row in 1 until rowCount) {
            val y = cTop + headerHeight + row * rowHeight
            canvas.drawLine(cLeft, y, cRight, y, strokePaint)
        }

        // 5. Draw cell content: Day numbers, Red X, Names
        val dayNumTextSize = (if (rowCount <= 5) 36f else 32f) * layoutConfig.cardScale
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = dayNumColor
            textSize = dayNumTextSize
            textAlign = Paint.Align.LEFT
        }

        val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E56B6F")
            alpha = 210 // 82% opacity
            style = Paint.Style.STROKE
            strokeWidth = 9.5f * layoutConfig.cardScale
            strokeCap = Paint.Cap.BUTT
        }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.parseColor("#1A1A1A")
            textAlign = Paint.Align.CENTER
        }

        val numPadLeft = 12f * layoutConfig.cardScale
        val numPadTop = 10f * layoutConfig.cardScale

        for (cell in cells) {
            val slot = cell.slotIndex
            val col = slot % 7
            val row = slot / 7
            if (row >= rowCount) continue

            val cellL = cLeft + col * colWidth
            val cellR = cellL + colWidth
            val cellT = cTop + headerHeight + row * rowHeight
            val cellB = cellT + rowHeight

            // Day number
            if (cell.dayNumber != null) {
                canvas.drawText(
                    cell.dayNumber.toString(),
                    cellL + numPadLeft,
                    cellT + numPadTop + dayNumTextSize * 0.88f,
                    numPaint
                )
            }

            // Usable area for X and Names (lower 77% of cell)
            val usableTop = cellT + (rowHeight * 0.23f)
            val usableBottom = cellB - (4f * layoutConfig.cardScale)
            val usableLeft = cellL + (6f * layoutConfig.cardScale)
            val usableRight = cellR - (6f * layoutConfig.cardScale)
            val usableWidth = max(1f, usableRight - usableLeft)
            val usableHeight = max(1f, usableBottom - usableTop)

            // Red X (under the names)
            if (cell.hasRedX) {
                val side = min(usableWidth, usableHeight) * 0.70f
                val centerX = (usableLeft + usableRight) / 2f
                val centerY = (usableTop + usableBottom) / 2f
                val xL = centerX - side / 2f
                val xR = centerX + side / 2f
                val xT = centerY - side / 2f
                val xB = centerY + side / 2f

                canvas.drawLine(xL, xT, xR, xB, xPaint)
                canvas.drawLine(xL, xB, xR, xT, xPaint)
            }

            // Names (layer above X) with 2-step dynamic auto-fit
            if (cell.names.isNotEmpty()) {
                val names = cell.names

                // Step 1: Vertical auto-fit by count
                var baseTextSize = (if (rowCount <= 5) 28f else 24f) * layoutConfig.cardScale
                val lineSpacingMult = 1.20f
                val minBaseSize = 11.5f * layoutConfig.cardScale

                while (names.size * baseTextSize * lineSpacingMult > usableHeight && baseTextSize > minBaseSize) {
                    baseTextSize -= 0.5f
                }

                // Step 2: Individual horizontal auto-fit per name
                val fittedSizes = FloatArray(names.size)
                val minLineSize = 10.0f * layoutConfig.cardScale

                for (i in names.indices) {
                    var lineSize = baseTextSize
                    namePaint.textSize = lineSize
                    while (namePaint.measureText(names[i]) > usableWidth && lineSize > minLineSize) {
                        lineSize -= 0.5f
                        namePaint.textSize = lineSize
                    }
                    fittedSizes[i] = lineSize
                }

                // Render lines aligned neatly without clipping accents
                val totalBlockH = (names.size - 1) * (baseTextSize * lineSpacingMult) + baseTextSize
                val startY = usableTop + max(2f, (usableHeight - totalBlockH) / 2f) + baseTextSize * 0.85f
                val centerX = (usableLeft + usableRight) / 2f

                for (i in names.indices) {
                    val lineY = startY + i * (baseTextSize * lineSpacingMult)
                    namePaint.textSize = fittedSizes[i]
                    canvas.drawText(names[i], centerX, lineY, namePaint)
                }
            }
        }

        canvas.restore()

        // 6. Outer Card Border
        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = accentColor
        strokePaint.strokeWidth = 3.5f * layoutConfig.cardScale
        cardRect.set(cLeft, cTop, cRight, cBottom)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, strokePaint)
    }

    fun calculateLogoBounds(layoutConfig: LayoutConfig, logoWidth: Int, logoHeight: Int): RectF {
        val aspect = if (logoHeight > 0) logoWidth.toFloat() / logoHeight.toFloat() else 1f
        val targetH = 205f * layoutConfig.logoScale
        val targetW = targetH * aspect
        val centerX = 540f + layoutConfig.logoOffsetX
        val centerY = 1750f + layoutConfig.logoOffsetY
        return RectF(
            centerX - targetW / 2f,
            centerY - targetH / 2f,
            centerX + targetW / 2f,
            centerY + targetH / 2f
        )
    }

    private fun drawLogo(canvas: Canvas, logoBitmap: Bitmap, layoutConfig: LayoutConfig) {
        val bounds = calculateLogoBounds(layoutConfig, logoBitmap.width, logoBitmap.height)
        srcRect.set(0, 0, logoBitmap.width, logoBitmap.height)

        when (layoutConfig.logoMode) {
            "REMOVE_WHITE" -> {
                val multiplyPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
                canvas.drawBitmap(logoBitmap, srcRect, bounds, multiplyPaint)
            }
            "CIRCLE_BADGE" -> {
                val radius = max(bounds.width(), bounds.height()) / 2f + 10f
                val centerX = bounds.centerX()
                val centerY = bounds.centerY()

                // Badge circular background
                fillPaint.reset()
                fillPaint.isAntiAlias = true
                fillPaint.color = Color.WHITE
                canvas.drawCircle(centerX, centerY, radius, fillPaint)

                // Badge border
                strokePaint.reset()
                strokePaint.isAntiAlias = true
                strokePaint.style = Paint.Style.STROKE
                strokePaint.color = layoutConfig.cardAccentColor
                strokePaint.strokeWidth = 3f
                canvas.drawCircle(centerX, centerY, radius, strokePaint)

                // Clipped logo
                canvas.save()
                clipPath.reset()
                clipPath.addCircle(centerX, centerY, radius - 2f, Path.Direction.CW)
                canvas.clipPath(clipPath)
                canvas.drawBitmap(logoBitmap, srcRect, bounds, bgPaint)
                canvas.restore()
            }
            else -> { // ORIGINAL
                bgPaint.reset()
                bgPaint.isAntiAlias = true
                bgPaint.isFilterBitmap = true
                canvas.drawBitmap(logoBitmap, srcRect, bounds, bgPaint)
            }
        }
    }

    fun getAutoFooterColor(themeId: String): Int {
        val theme = ThemeCatalog.getThemeById(themeId)
        val baseHex = theme?.palette?.backgroundBaseHex ?: "#FFFFFF"
        val baseColor = try {
            Color.parseColor(baseHex)
        } catch (_: Exception) {
            Color.WHITE
        }
        val luminance = ColorUtils.calculateLuminance(baseColor)
        return if (luminance < 0.35) {
            Color.parseColor("#FFE082")
        } else {
            try {
                Color.parseColor(theme?.palette?.monthColorHex ?: "#5E2326")
            } catch (_: Exception) {
                Color.parseColor("#5E2326")
            }
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    currentLine = StringBuilder(candidate)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                    }
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }

    fun calculateFooterBounds(layoutConfig: LayoutConfig, context: Context? = null): RectF {
        val text = layoutConfig.footerMessage.ifBlank { "Recado" }
        val textSize = 38f * layoutConfig.footerScale
        val localPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            if (context != null) {
                typeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)
            } else {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }

        val maxWidth = 900f
        val lines = wrapText(text, localPaint, maxWidth)
        val lineHeight = textSize * 1.35f
        val totalHeight = max(textSize, (lines.size - 1) * lineHeight + textSize)
        val maxLineWidth = lines.maxOfOrNull { localPaint.measureText(it) } ?: 200f

        val centerX = 540f + layoutConfig.footerOffsetX
        val centerY = 1750f + layoutConfig.footerOffsetY
        val halfW = (maxLineWidth / 2f) + 24f
        val halfH = (totalHeight / 2f) + 16f
        return RectF(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH)
    }

    private fun drawFooterMessage(canvas: Canvas, context: Context, layoutConfig: LayoutConfig) {
        if (!layoutConfig.showFooterMessage || layoutConfig.footerMessage.isBlank()) return

        val color = if (layoutConfig.footerColor != 0) {
            layoutConfig.footerColor
        } else {
            getAutoFooterColor(layoutConfig.selectedThemeId)
        }

        val textSize = 38f * layoutConfig.footerScale
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.isSubpixelText = true
        textPaint.typeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)
        textPaint.textSize = textSize
        textPaint.color = color
        textPaint.textAlign = Paint.Align.CENTER

        // Subtle soft shadow for contrast and legibility over detailed backgrounds
        textPaint.setShadowLayer(4f, 0f, 2f, Color.argb(45, 0, 0, 0))

        val maxWidth = 900f
        val lines = wrapText(layoutConfig.footerMessage, textPaint, maxWidth)
        val lineHeight = textSize * 1.35f
        val totalHeight = (lines.size - 1) * lineHeight + textSize

        val centerX = 540f + layoutConfig.footerOffsetX
        val centerY = 1750f + layoutConfig.footerOffsetY

        val fontMetrics = textPaint.fontMetrics
        val textAscent = fontMetrics.ascent
        val textDescent = fontMetrics.descent
        val startY = centerY - (totalHeight / 2f) - ((textAscent + textDescent) / 2f)

        for ((i, line) in lines.withIndex()) {
            val y = startY + (i * lineHeight)
            canvas.drawText(line, centerX, y, textPaint)
        }
        textPaint.clearShadowLayer()
    }

    private fun drawEditModeHighlights(
        canvas: Canvas,
        layoutConfig: LayoutConfig,
        cardBounds: RectF,
        selectedElement: EditableElement,
        monthText: String,
        monthTextSize: Float,
        yearText: String,
        yearTextSize: Float,
        monthTypeface: Typeface,
        yearTypeface: Typeface,
        logoBitmap: Bitmap? = null,
        context: Context? = null
    ) {
        val activeColor = Color.parseColor("#E91E63")
        val inactiveColor = Color.parseColor("#99D81B60")

        // 1. Month bounding box
        textPaint.reset()
        textPaint.typeface = monthTypeface
        textPaint.textSize = monthTextSize
        val monthW = textPaint.measureText(monthText)
        val monthPad = 20f
        val monthRect = RectF(
            layoutConfig.monthX - (monthW / 2f) - monthPad,
            layoutConfig.monthY - monthTextSize - monthPad * 0.5f,
            layoutConfig.monthX + (monthW / 2f) + monthPad,
            layoutConfig.monthY + monthPad
        )
        dashedPaint.color = if (selectedElement == EditableElement.MONTH) activeColor else inactiveColor
        dashedPaint.strokeWidth = if (selectedElement == EditableElement.MONTH) 4.5f else 2.5f
        canvas.drawRoundRect(monthRect, 12f, 12f, dashedPaint)

        // 2. Year + Swoosh bounding box
        textPaint.reset()
        textPaint.typeface = yearTypeface
        textPaint.textSize = yearTextSize
        val yearW = textPaint.measureText(yearText)
        val yearPad = 25f
        val swooshExtra = 40f * layoutConfig.yearScale
        val yearRect = RectF(
            layoutConfig.yearX - max(yearW / 2f, 180f * layoutConfig.yearScale) - yearPad,
            layoutConfig.yearY - yearTextSize * 0.85f - yearPad,
            layoutConfig.yearX + max(yearW / 2f, 180f * layoutConfig.yearScale) + yearPad,
            layoutConfig.yearY + swooshExtra + yearPad
        )
        dashedPaint.color = if (selectedElement == EditableElement.YEAR) activeColor else inactiveColor
        dashedPaint.strokeWidth = if (selectedElement == EditableElement.YEAR) 4.5f else 2.5f
        canvas.drawRoundRect(yearRect, 16f, 16f, dashedPaint)

        // 3. Card bounding box
        dashedPaint.color = if (selectedElement == EditableElement.GRID_CARD) activeColor else inactiveColor
        dashedPaint.strokeWidth = if (selectedElement == EditableElement.GRID_CARD) 4.5f else 2.5f
        val cardOutRect = RectF(cardBounds.left - 6f, cardBounds.top - 6f, cardBounds.right + 6f, cardBounds.bottom + 6f)
        canvas.drawRoundRect(cardOutRect, 42f * layoutConfig.cardScale, 42f * layoutConfig.cardScale, dashedPaint)

        // 4. Logo bounding box (if enabled)
        if (layoutConfig.logoEnabled) {
            val logoW = logoBitmap?.width ?: 200
            val logoH = logoBitmap?.height ?: 150
            val logoBounds = calculateLogoBounds(layoutConfig, logoW, logoH)
            val logoPad = 14f
            val logoRect = RectF(
                logoBounds.left - logoPad,
                logoBounds.top - logoPad,
                logoBounds.right + logoPad,
                logoBounds.bottom + logoPad
            )
            dashedPaint.color = if (selectedElement == EditableElement.LOGO) activeColor else inactiveColor
            dashedPaint.strokeWidth = if (selectedElement == EditableElement.LOGO) 4.5f else 2.5f
            canvas.drawRoundRect(logoRect, 16f, 16f, dashedPaint)
        }

        // 5. Footer Message bounding box (if enabled or selected)
        if ((layoutConfig.showFooterMessage && layoutConfig.footerMessage.isNotBlank()) || selectedElement == EditableElement.FOOTER_MESSAGE) {
            val footerBounds = calculateFooterBounds(layoutConfig, context)
            val footerPad = 14f
            val footerRect = RectF(
                footerBounds.left - footerPad,
                footerBounds.top - footerPad,
                footerBounds.right + footerPad,
                footerBounds.bottom + footerPad
            )
            dashedPaint.color = if (selectedElement == EditableElement.FOOTER_MESSAGE) activeColor else inactiveColor
            dashedPaint.strokeWidth = if (selectedElement == EditableElement.FOOTER_MESSAGE) 4.5f else 2.5f
            canvas.drawRoundRect(footerRect, 16f, 16f, dashedPaint)
        }
    }

    // Touch hit testing helpers
    fun getHitElement(x: Float, y: Float, layoutConfig: LayoutConfig): EditableElement {
        // 1. Check Logo zone first (if enabled)
        if (layoutConfig.logoEnabled) {
            val centerX = 540f + layoutConfig.logoOffsetX
            val centerY = 1750f + layoutConfig.logoOffsetY
            val halfW = max(260f * layoutConfig.logoScale, 160f) + 40f
            val halfH = max(115f * layoutConfig.logoScale, 80f) + 40f
            if (x in (centerX - halfW)..(centerX + halfW) && y in (centerY - halfH)..(centerY + halfH)) {
                return EditableElement.LOGO
            }
        }

        // 2. Check Footer Message zone (if enabled and not blank)
        if (layoutConfig.showFooterMessage && layoutConfig.footerMessage.isNotBlank()) {
            val footerBounds = calculateFooterBounds(layoutConfig, null)
            val pad = 24f
            if (x in (footerBounds.left - pad)..(footerBounds.right + pad) &&
                y in (footerBounds.top - pad)..(footerBounds.bottom + pad)
            ) {
                return EditableElement.FOOTER_MESSAGE
            }
        }

        // 3. Check Month zone
        val monthPad = 50f
        if (x in (layoutConfig.monthX - 300f)..(layoutConfig.monthX + 300f) &&
            y in (layoutConfig.monthY - 100f)..(layoutConfig.monthY + monthPad)
        ) {
            return EditableElement.MONTH
        }

        // 3. Check Year zone
        if (x in (layoutConfig.yearX - 250f)..(layoutConfig.yearX + 250f) &&
            y in (layoutConfig.yearY - 120f)..(layoutConfig.yearY + 80f)
        ) {
            return EditableElement.YEAR
        }

        // 4. Check Card zone
        val cardBounds = calculateCardBounds(layoutConfig)
        if (cardBounds.contains(x, y)) {
            return EditableElement.GRID_CARD
        }

        return EditableElement.NONE
    }

    fun getCellAt(x: Float, y: Float, layoutConfig: LayoutConfig, rowCount: Int): Int? {
        val cardBounds = calculateCardBounds(layoutConfig)
        val headerHeight = 96f * layoutConfig.cardScale
        val gridTop = cardBounds.top + headerHeight
        val gridBottom = cardBounds.bottom

        if (x !in cardBounds.left..cardBounds.right || y !in gridTop..gridBottom) {
            return null
        }

        val colWidth = (cardBounds.right - cardBounds.left) / 7f
        val gridHeight = gridBottom - gridTop
        val rowHeight = gridHeight / rowCount.toFloat()

        val col = ((x - cardBounds.left) / colWidth).toInt().coerceIn(0, 6)
        val row = ((y - gridTop) / rowHeight).toInt().coerceIn(0, rowCount - 1)

        val slotIndex = row * 7 + col
        return if (slotIndex in 0 until (rowCount * 7)) slotIndex else null
    }
}
