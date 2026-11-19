package com.salu.calendario

import android.graphics.Color
import org.json.JSONObject

data class LayoutConfig(
    // Month position, scale, font and color
    val monthX: Float = 540f,
    val monthY: Float = 115f,
    val monthOffsetX: Float = monthX - 540f,
    val monthOffsetY: Float = monthY - 115f,
    val monthScale: Float = 1.0f,
    val monthFont: HeaderFontOption = HeaderFontOption.CINZEL_BOLD,
    val monthColor: Int = 0xFF5E2326.toInt(), // Default: #5E2326

    // Year position, scale, font and color
    val yearX: Float = 540f,
    val yearY: Float = 225f,
    val yearOffsetX: Float = yearX - 540f,
    val yearOffsetY: Float = yearY - 225f,
    val yearScale: Float = 1.0f,
    val yearFont: HeaderFontOption = HeaderFontOption.FREDOKA_BOLD,
    val yearColor: Int = 0xFFE64886.toInt(), // Default: #E64886

    // Grid Card position, scale, offsets and accent color
    val cardLeft: Float = 20f,
    val cardRight: Float = 1060f,
    val cardTop: Float = 278f,
    val cardBottom: Float = 1612f,
    val cardScale: Float = 1.0f,
    val cardOffsetX: Float = 0f,
    val cardOffsetY: Float = 0f,
    val cardAccentColor: Int = 0xFFE89BAE.toInt() // Default: #E89BAE
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("monthX", monthX.toDouble())
        json.put("monthY", monthY.toDouble())
        json.put("monthOffsetX", monthOffsetX.toDouble())
        json.put("monthOffsetY", monthOffsetY.toDouble())
        json.put("monthScale", monthScale.toDouble())
        json.put("monthFont", monthFont.name)
        json.put("monthColor", String.format("#%08X", monthColor))

        json.put("yearX", yearX.toDouble())
        json.put("yearY", yearY.toDouble())
        json.put("yearOffsetX", yearOffsetX.toDouble())
        json.put("yearOffsetY", yearOffsetY.toDouble())
        json.put("yearScale", yearScale.toDouble())
        json.put("yearFont", yearFont.name)
        json.put("yearColor", String.format("#%08X", yearColor))

        json.put("cardLeft", cardLeft.toDouble())
        json.put("cardRight", cardRight.toDouble())
        json.put("cardTop", cardTop.toDouble())
        json.put("cardBottom", cardBottom.toDouble())
        json.put("cardScale", cardScale.toDouble())
        json.put("cardOffsetX", cardOffsetX.toDouble())
        json.put("cardOffsetY", cardOffsetY.toDouble())
        json.put("cardAccentColor", String.format("#%08X", cardAccentColor))
        return json.toString()
    }

    companion object {
        val DEFAULT = LayoutConfig()

        fun fromJson(jsonStr: String?): LayoutConfig {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)

                // Safe fallback: read monthOffsetX if available, otherwise derive from monthX
                val mX = if (json.has("monthOffsetX")) {
                    (540.0 + json.optDouble("monthOffsetX", 0.0)).toFloat()
                } else {
                    json.optDouble("monthX", 540.0).toFloat()
                }
                val mY = if (json.has("monthOffsetY")) {
                    (115.0 + json.optDouble("monthOffsetY", 0.0)).toFloat()
                } else {
                    json.optDouble("monthY", 115.0).toFloat()
                }
                val mOffX = json.optDouble("monthOffsetX", (mX - 540.0)).toFloat()
                val mOffY = json.optDouble("monthOffsetY", (mY - 115.0)).toFloat()

                // Safe fallback: read yearOffsetX if available, otherwise derive from yearX
                val yX = if (json.has("yearOffsetX")) {
                    (540.0 + json.optDouble("yearOffsetX", 0.0)).toFloat()
                } else {
                    json.optDouble("yearX", 540.0).toFloat()
                }
                val yY = if (json.has("yearOffsetY")) {
                    (225.0 + json.optDouble("yearOffsetY", 0.0)).toFloat()
                } else {
                    json.optDouble("yearY", 225.0).toFloat()
                }
                val yOffX = json.optDouble("yearOffsetX", (yX - 540.0)).toFloat()
                val yOffY = json.optDouble("yearOffsetY", (yY - 225.0)).toFloat()

                LayoutConfig(
                    monthX = mX,
                    monthY = mY,
                    monthOffsetX = mOffX,
                    monthOffsetY = mOffY,
                    monthScale = json.optDouble("monthScale", 1.0).toFloat(),
                    monthFont = try {
                        HeaderFontOption.valueOf(json.optString("monthFont", HeaderFontOption.CINZEL_BOLD.name))
                    } catch (e: Exception) {
                        HeaderFontOption.CINZEL_BOLD
                    },
                    monthColor = parseColorSafe(json.optString("monthColor"), 0xFF5E2326.toInt()),

                    yearX = yX,
                    yearY = yY,
                    yearOffsetX = yOffX,
                    yearOffsetY = yOffY,
                    yearScale = json.optDouble("yearScale", 1.0).toFloat(),
                    yearFont = try {
                        HeaderFontOption.valueOf(json.optString("yearFont", HeaderFontOption.FREDOKA_BOLD.name))
                    } catch (e: Exception) {
                        HeaderFontOption.FREDOKA_BOLD
                    },
                    yearColor = parseColorSafe(json.optString("yearColor"), 0xFFE64886.toInt()),

                    cardLeft = json.optDouble("cardLeft", 20.0).toFloat(),
                    cardRight = json.optDouble("cardRight", 1060.0).toFloat(),
                    cardTop = json.optDouble("cardTop", 278.0).toFloat(),
                    cardBottom = json.optDouble("cardBottom", 1612.0).toFloat(),
                    cardScale = json.optDouble("cardScale", 1.0).toFloat(),
                    cardOffsetX = json.optDouble("cardOffsetX", 0.0).toFloat(),
                    cardOffsetY = json.optDouble("cardOffsetY", 0.0).toFloat(),
                    cardAccentColor = parseColorSafe(json.optString("cardAccentColor"), 0xFFE89BAE.toInt())
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }

        private fun parseColorSafe(colorStr: String?, defaultColor: Int): Int {
            if (colorStr.isNullOrBlank()) return defaultColor
            return try {
                Color.parseColor(colorStr)
            } catch (e: Exception) {
                defaultColor
            }
        }
    }
}
