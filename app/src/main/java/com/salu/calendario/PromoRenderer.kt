package com.salu.calendario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min

object PromoRenderer {
    const val VIRTUAL_WIDTH = 1080f
    const val VIRTUAL_HEIGHT = 1920f

    // Reusable objects for optimal performance
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val pillFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val bodyTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)

    private val srcRect = Rect()
    private val dstRect = RectF()
    private val cardRect = RectF()
    private val pillRect = RectF()
    private val itemRect = RectF()
    private val circleClipPath = Path()

    fun drawPromo(
        canvas: Canvas,
        context: Context,
        promoDraft: PromoDraft,
        layoutConfig: LayoutConfig,
        bgBitmapOverride: Bitmap? = null
    ) {
        // 1. Draw Background (from ThemeCatalog or override)
        val bgBitmap = bgBitmapOverride ?: ThemeCatalog.getThemeBitmap(context, layoutConfig.selectedThemeId)
        srcRect.set(0, 0, bgBitmap.width, bgBitmap.height)
        dstRect.set(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT)
        canvas.drawBitmap(bgBitmap, srcRect, dstRect, bgPaint)

        // 2. Resolve Theme Palette and Luminance
        val theme = ThemeCatalog.getThemeById(layoutConfig.selectedThemeId)
            ?: ThemeCatalog.getThemeById("floral_default")
            ?: ThemeCatalog.getThemesByCategory(ThemeCategory.ALL).first()
        val palette = theme.palette

        val bgBaseColor = try { Color.parseColor(palette.backgroundBaseHex) } catch (_: Exception) { Color.WHITE }
        val isDark = ColorUtils.calculateLuminance(bgBaseColor) < 0.35f

        val accentColor = try { Color.parseColor(palette.calendarAccentHex) } catch (_: Exception) { Color.parseColor("#E89BAE") }
        val yearColor = try { Color.parseColor(palette.yearColorHex) } catch (_: Exception) { Color.parseColor("#B82B62") }
        val monthColor = try { Color.parseColor(palette.monthColorHex) } catch (_: Exception) { Color.parseColor("#5E2326") }

        val primaryTextColor = if (isDark) Color.WHITE else monthColor
        val secondaryTextColor = if (isDark) Color.parseColor("#E4E4E7") else Color.parseColor("#6E5055")
        val highlightColor = if (isDark) {
            if (ColorUtils.calculateContrast(yearColor, Color.parseColor("#161618")) < 3.5) Color.parseColor("#F5C869") else yearColor
        } else {
            yearColor
        }

        // 3. Safe Margins for photographic/ornamental frames (~110px horizontal, ~180px vertical)
        val cardLeft = 110f
        val cardRight = 970f
        val cardTop = 180f
        val cardBottom = 1740f
        val cardWidth = cardRight - cardLeft
        val cornerRadius = 36f

        // 4. Draw Translucent Elegant Card
        cardRect.set(cardLeft, cardTop, cardRight, cardBottom)

        cardFillPaint.color = if (isDark) {
            Color.argb(240, 22, 22, 24)
        } else {
            Color.argb(245, 255, 255, 255)
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardFillPaint)

        cardStrokePaint.color = if (isDark) {
            Color.argb(160, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        } else {
            accentColor
        }
        cardStrokePaint.strokeWidth = 3.5f
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardStrokePaint)

        // 5. Draw Header Section (Logo, Badge, Title, Subtitle, Divider)
        var cursorY = cardTop + 42f
        val centerX = 540f

        // Optional Logo at top
        val logoBitmap = if (promoDraft.showLogo) LogoManager.getLogoBitmap(context) else null
        if (promoDraft.showLogo && logoBitmap != null) {
            val logoAspect = if (logoBitmap.height > 0) logoBitmap.width.toFloat() / logoBitmap.height.toFloat() else 1f
            val targetH = 115f
            val targetW = min(360f, targetH * logoAspect)
            val logoRect = RectF(centerX - targetW / 2f, cursorY, centerX + targetW / 2f, cursorY + targetH)

            drawPromoLogo(canvas, logoBitmap, logoRect, layoutConfig.logoMode)
            cursorY += targetH + 20f
        }

        // Pill Badge (e.g. "ÚLTIMOS HORÁRIOS")
        if (promoDraft.badgeText.isNotBlank()) {
            val badgeText = promoDraft.badgeText.trim().uppercase()
            val badgeTypeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)
            val badgeTextSize = 24f

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = badgeTypeface
            textPaint.textSize = badgeTextSize
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = highlightColor

            val badgeTextWidth = textPaint.measureText(badgeText)
            val pillWidth = min(cardWidth - 80f, badgeTextWidth + 56f)
            val pillHeight = 46f

            pillRect.set(centerX - pillWidth / 2f, cursorY, centerX + pillWidth / 2f, cursorY + pillHeight)

            pillFillPaint.color = if (isDark) {
                Color.argb(55, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor))
            } else {
                Color.argb(35, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor))
            }
            canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillFillPaint)

            pillStrokePaint.color = highlightColor
            pillStrokePaint.strokeWidth = 2f
            canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillStrokePaint)

            val textBaseline = pillRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(badgeText, centerX, textBaseline, textPaint)

            cursorY += pillHeight + 20f
        }

        // Main Title (e.g. "Agenda Aberta")
        if (promoDraft.title.isNotBlank()) {
            val titleTypeface = FontManager.getTypeface(context, HeaderFontOption.PLAYFAIR_DISPLAY_BOLD)
            var titleSize = 62f

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = titleTypeface
            textPaint.textSize = titleSize
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = primaryTextColor

            val maxTitleWidth = cardWidth - 80f
            val measuredW = textPaint.measureText(promoDraft.title)
            if (measuredW > maxTitleWidth && maxTitleWidth > 0f) {
                titleSize *= (maxTitleWidth / measuredW)
                textPaint.textSize = titleSize
            }

            cursorY += titleSize * 0.92f
            canvas.drawText(promoDraft.title, centerX, cursorY, textPaint)
            cursorY += 18f
        }

        // Subtitle (e.g. "Garanta seu momento de autocuidado...")
        if (promoDraft.subtitle.isNotBlank()) {
            val subTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            var subSize = 29f

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = subTypeface
            textPaint.textSize = subSize
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = secondaryTextColor

            val maxSubWidth = cardWidth - 90f
            val measuredW = textPaint.measureText(promoDraft.subtitle)
            if (measuredW > maxSubWidth && maxSubWidth > 0f) {
                subSize *= (maxSubWidth / measuredW)
                textPaint.textSize = subSize
            }

            cursorY += subSize * 0.92f
            canvas.drawText(promoDraft.subtitle, centerX, cursorY, textPaint)
            cursorY += 24f
        }

        // Delicate ornamental divider
        val divWidth = 520f
        val divStart = centerX - divWidth / 2f
        val divEnd = centerX + divWidth / 2f
        linePaint.reset()
        linePaint.isAntiAlias = true
        linePaint.strokeWidth = 2f
        linePaint.shader = LinearGradient(
            divStart, cursorY,
            divEnd, cursorY,
            intArrayOf(
                Color.TRANSPARENT,
                accentColor,
                accentColor,
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.25f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(divStart, cursorY, divEnd, cursorY, linePaint)
        linePaint.shader = null
        cursorY += 30f

        // 6. Reserve and Draw Footer CTA at the Bottom
        val footerY = cardBottom - 48f
        if (promoDraft.footerCta.isNotBlank()) {
            val footerText = promoDraft.footerCta.trim()
            val footerTypeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)
            var footerSize = 38f

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = footerTypeface
            textPaint.textSize = footerSize
            textPaint.textAlign = Paint.Align.CENTER

            // Luminance contrast check for footer
            textPaint.color = if (isDark) {
                val contrastRatio = ColorUtils.calculateContrast(highlightColor, Color.parseColor("#181818"))
                if (contrastRatio < 3.5) Color.parseColor("#F5C869") else highlightColor
            } else {
                val contrastRatio = ColorUtils.calculateContrast(highlightColor, Color.WHITE)
                if (contrastRatio < 3.0) monthColor else highlightColor
            }

            val maxFooterWidth = cardWidth - 80f
            val measuredW = textPaint.measureText(footerText)
            if (measuredW > maxFooterWidth && maxFooterWidth > 0f) {
                footerSize *= (maxFooterWidth / measuredW)
                textPaint.textSize = footerSize
            }

            canvas.drawText(footerText, centerX, footerY, textPaint)
        }

        // 7. Render Template-Specific Body Items
        val usableTop = cursorY
        val usableBottom = footerY - 54f
        val availableHeight = max(100f, usableBottom - usableTop)
        val items = promoDraft.items.take(PromoDraft.MAX_ITEMS)

        if (items.isNotEmpty()) {
            when (promoDraft.templateType) {
                PromoTemplateType.AGENDA_ABERTA -> {
                    drawAgendaAbertaBody(
                        canvas = canvas,
                        context = context,
                        items = items,
                        left = cardLeft + 36f,
                        right = cardRight - 36f,
                        top = usableTop,
                        availableHeight = availableHeight,
                        isDark = isDark,
                        primaryColor = primaryTextColor,
                        highlightColor = highlightColor,
                        accentColor = accentColor
                    )
                }
                PromoTemplateType.TABELA_PRECOS -> {
                    drawTabelaPrecosBody(
                        canvas = canvas,
                        context = context,
                        items = items,
                        left = cardLeft + 36f,
                        right = cardRight - 36f,
                        top = usableTop,
                        availableHeight = availableHeight,
                        isDark = isDark,
                        primaryColor = primaryTextColor,
                        highlightColor = highlightColor,
                        accentColor = accentColor
                    )
                }
                PromoTemplateType.AVISO_REGRAS -> {
                    drawAvisoRegrasBody(
                        canvas = canvas,
                        context = context,
                        items = items,
                        left = cardLeft + 36f,
                        right = cardRight - 36f,
                        top = usableTop,
                        availableHeight = availableHeight,
                        isDark = isDark,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        accentColor = accentColor,
                        highlightColor = highlightColor
                    )
                }
            }
        }
    }

    private fun drawAgendaAbertaBody(
        canvas: Canvas,
        context: Context,
        items: List<PromoLineItem>,
        left: Float,
        right: Float,
        top: Float,
        availableHeight: Float,
        isDark: Boolean,
        primaryColor: Int,
        highlightColor: Int,
        accentColor: Int
    ) {
        val count = items.size
        val itemHeight = if (count <= 4) 124f else 108f
        val spacing = if (count <= 4) 22f else 16f
        val totalHeight = count * itemHeight + (count - 1) * spacing
        val startY = if (totalHeight < availableHeight) top + (availableHeight - totalHeight) / 2f else top

        val labelTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val valueTypeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)

        for (i in items.indices) {
            val item = items[i]
            val itemTop = startY + i * (itemHeight + spacing)
            val itemBottom = itemTop + itemHeight

            itemRect.set(left, itemTop, right, itemBottom)

            // Item card background
            cardFillPaint.color = if (isDark) {
                Color.argb(45, 255, 255, 255)
            } else {
                Color.argb(35, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
            }
            canvas.drawRoundRect(itemRect, 20f, 20f, cardFillPaint)

            cardStrokePaint.color = if (isDark) Color.argb(90, 255, 255, 255) else accentColor
            cardStrokePaint.strokeWidth = 1.8f
            canvas.drawRoundRect(itemRect, 20f, 20f, cardStrokePaint)

            // Label (Left: Dia / Data)
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = labelTypeface
            textPaint.textSize = 34f
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = primaryColor

            val labelX = left + 28f
            val centerY = (itemTop + itemBottom) / 2f
            val textBaseline = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(item.label, labelX, textBaseline, textPaint)

            // Value (Right: Horários) in pill container or accent text
            if (item.value.isNotBlank()) {
                textPaint.typeface = valueTypeface
                textPaint.textSize = 32f
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = highlightColor

                val valueX = right - 28f
                val valueBaseline = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
                canvas.drawText(item.value, valueX, valueBaseline, textPaint)
            }
        }
    }

    private fun drawTabelaPrecosBody(
        canvas: Canvas,
        context: Context,
        items: List<PromoLineItem>,
        left: Float,
        right: Float,
        top: Float,
        availableHeight: Float,
        isDark: Boolean,
        primaryColor: Int,
        highlightColor: Int,
        accentColor: Int
    ) {
        val count = items.size
        val rowHeight = if (count <= 4) 100f else if (count == 5) 90f else 82f
        val totalHeight = count * rowHeight
        val startY = if (totalHeight < availableHeight) top + (availableHeight - totalHeight) / 2f else top

        val labelTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val priceTypeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.argb(130, 255, 255, 255) else Color.argb(130, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }

        for (i in items.indices) {
            val item = items[i]
            val rowCenterY = startY + (i + 0.5f) * rowHeight

            // Right: Price (fixed right alignment)
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.isSubpixelText = true
            textPaint.typeface = priceTypeface
            textPaint.textSize = 38f
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = highlightColor

            val priceX = right - 12f
            val priceBaseline = rowCenterY - ((textPaint.descent() + textPaint.ascent()) / 2f)
            val priceWidth = if (item.value.isNotBlank()) textPaint.measureText(item.value) else 0f
            if (item.value.isNotBlank()) {
                canvas.drawText(item.value, priceX, priceBaseline, textPaint)
            }

            // Left: Service Label
            textPaint.typeface = labelTypeface
            var labelSize = 34f
            textPaint.textSize = labelSize
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = primaryColor

            val labelX = left + 12f
            val maxLabelWidth = (right - left) - priceWidth - 70f
            var measuredLabelW = textPaint.measureText(item.label)
            if (measuredLabelW > maxLabelWidth && maxLabelWidth > 0f) {
                labelSize *= (maxLabelWidth / measuredLabelW)
                textPaint.textSize = labelSize
                measuredLabelW = textPaint.measureText(item.label)
            }
            val labelBaseline = rowCenterY - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(item.label, labelX, labelBaseline, textPaint)

            // Dotted leader line between label and price
            if (item.value.isNotBlank()) {
                val dotsStart = labelX + measuredLabelW + 16f
                val dotsEnd = priceX - priceWidth - 16f
                if (dotsEnd > dotsStart + 24f) {
                    var dotX = dotsStart
                    val dotStep = 20f
                    val dotBaseline = rowCenterY - ((dotPaint.descent() + dotPaint.ascent()) / 2f)
                    while (dotX < dotsEnd) {
                        canvas.drawText("·", dotX, dotBaseline, dotPaint)
                        dotX += dotStep
                    }
                }
            }
        }
    }

    private fun drawAvisoRegrasBody(
        canvas: Canvas,
        context: Context,
        items: List<PromoLineItem>,
        left: Float,
        right: Float,
        top: Float,
        availableHeight: Float,
        isDark: Boolean,
        primaryColor: Int,
        secondaryColor: Int,
        accentColor: Int,
        highlightColor: Int
    ) {
        val count = items.size
        val itemHeight = if (count <= 3) 136f else if (count <= 4) 120f else 105f
        val spacing = if (count <= 3) 24f else 18f
        val totalHeight = count * itemHeight + (count - 1) * spacing
        val startY = if (totalHeight < availableHeight) top + (availableHeight - totalHeight) / 2f else top

        val titleTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val valueTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val badgeTypeface = FontManager.getTypeface(context, HeaderFontOption.FREDOKA_BOLD)

        val bulletRadius = 22f

        for (i in items.indices) {
            val item = items[i]
            val itemTop = startY + i * (itemHeight + spacing)
            val itemCenterY = itemTop + itemHeight / 2f

            // Numbered bullet circle on left
            val bulletCenterX = left + bulletRadius + 8f
            val bulletCenterY = if (item.value.isNotBlank()) itemTop + bulletRadius + 8f else itemCenterY

            pillFillPaint.color = if (isDark) {
                Color.argb(70, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor))
            } else {
                Color.argb(40, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor))
            }
            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletRadius, pillFillPaint)

            pillStrokePaint.color = highlightColor
            pillStrokePaint.strokeWidth = 1.8f
            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletRadius, pillStrokePaint)

            // Number inside bullet
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.typeface = badgeTypeface
            textPaint.textSize = 24f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = highlightColor
            val bulletBaseline = bulletCenterY - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText("${i + 1}", bulletCenterX, bulletBaseline, textPaint)

            // Text block to the right of bullet
            val contentLeft = bulletCenterX + bulletRadius + 20f
            val contentRight = right - 12f
            val maxTextWidth = contentRight - contentLeft

            // Rule Title (Label)
            textPaint.typeface = titleTypeface
            var titleSize = 34f
            textPaint.textSize = titleSize
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = primaryColor

            val measuredTitleW = textPaint.measureText(item.label)
            if (measuredTitleW > maxTextWidth && maxTextWidth > 0f) {
                titleSize *= (maxTextWidth / measuredTitleW)
                textPaint.textSize = titleSize
            }
            val titleY = if (item.value.isNotBlank()) itemTop + titleSize else itemCenterY - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(item.label, contentLeft, titleY, textPaint)

            // Rule Detail (Value)
            if (item.value.isNotBlank()) {
                bodyTextPaint.reset()
                bodyTextPaint.isAntiAlias = true
                bodyTextPaint.isSubpixelText = true
                bodyTextPaint.typeface = valueTypeface
                bodyTextPaint.textSize = 27f
                bodyTextPaint.color = secondaryColor

                val detailY = titleY + 34f
                val measuredDetailW = bodyTextPaint.measureText(item.value)
                if (measuredDetailW <= maxTextWidth) {
                    canvas.drawText(item.value, contentLeft, detailY, bodyTextPaint)
                } else {
                    // Wrap if long
                    val staticLayout = StaticLayout.Builder.obtain(
                        item.value, 0, item.value.length,
                        bodyTextPaint, maxTextWidth.toInt()
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.15f)
                        .setMaxLines(2)
                        .build()

                    canvas.save()
                    canvas.translate(contentLeft, titleY + 8f)
                    staticLayout.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun drawPromoLogo(canvas: Canvas, logoBitmap: Bitmap, bounds: RectF, logoMode: String) {
        srcRect.set(0, 0, logoBitmap.width, logoBitmap.height)

        when (logoMode) {
            "REMOVE_WHITE" -> {
                val multiplyPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
                canvas.drawBitmap(logoBitmap, srcRect, bounds, multiplyPaint)
            }
            "CIRCLE" -> {
                val layerId = canvas.saveLayer(bounds, null)
                circleClipPath.reset()
                val radius = min(bounds.width(), bounds.height()) / 2f
                circleClipPath.addCircle(bounds.centerX(), bounds.centerY(), radius, Path.Direction.CW)
                canvas.clipPath(circleClipPath)
                canvas.drawBitmap(logoBitmap, srcRect, bounds, bgPaint)
                canvas.restoreToCount(layerId)
            }
            else -> {
                // "ORIGINAL"
                canvas.drawBitmap(logoBitmap, srcRect, bounds, bgPaint)
            }
        }
    }
}
