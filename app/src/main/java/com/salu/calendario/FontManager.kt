package com.salu.calendario

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import java.util.concurrent.ConcurrentHashMap

enum class HeaderFontOption(val displayName: String) {
    CINZEL_BOLD("Cinzel"),
    PLAYFAIR_DISPLAY_BOLD("Playfair"),
    FREDOKA_BOLD("Fredoka"),
    SYSTEM_DEFAULT("Padrão")
}

object FontManager {
    private val typefaceCache = ConcurrentHashMap<HeaderFontOption, Typeface>()

    fun getTypeface(context: Context, option: HeaderFontOption): Typeface {
        return typefaceCache.computeIfAbsent(option) {
            loadTypefaceSafely(context, option)
        }
    }

    private fun loadTypefaceSafely(context: Context, option: HeaderFontOption): Typeface {
        return try {
            when (option) {
                HeaderFontOption.CINZEL_BOLD -> {
                    val fontRes = R.font.cinzel_bold
                    ResourcesCompat.getFont(context, fontRes)
                        ?: Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                HeaderFontOption.PLAYFAIR_DISPLAY_BOLD -> {
                    val fontRes = R.font.playfair_display_bold
                    ResourcesCompat.getFont(context, fontRes)
                        ?: Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                HeaderFontOption.FREDOKA_BOLD -> {
                    val fontRes = R.font.fredoka_bold
                    ResourcesCompat.getFont(context, fontRes)
                        ?: Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                HeaderFontOption.SYSTEM_DEFAULT -> {
                    Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
        } catch (e: Exception) {
            // Safe fallback: never crash due to fonts
            when (option) {
                HeaderFontOption.FREDOKA_BOLD -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                HeaderFontOption.CINZEL_BOLD,
                HeaderFontOption.PLAYFAIR_DISPLAY_BOLD -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
                HeaderFontOption.SYSTEM_DEFAULT -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }
    }
}
