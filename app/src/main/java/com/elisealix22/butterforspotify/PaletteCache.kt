package com.elisealix22.butterforspotify

import androidx.collection.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import kotlin.random.Random

object PaletteCache {
    private const val MAX_PALETTES = 500
    private val lruCache = LruCache<String, Palette>(MAX_PALETTES)
    private val fallbackColors = listOf(
        ThemeColor.Tangerine,
        ThemeColor.Orange,
        ThemeColor.Citrus,
        ThemeColor.Blue,
        ThemeColor.Pink
    )

    fun put(uri: String?, palette: Palette): Boolean = uri.let {
        if (it != null) {
            lruCache.put(it, palette)
            true
        } else {
            false
        }
    }

    fun get(uri: String?): Palette? = uri?.let { lruCache[it] }

    fun randomFallbackColor() = fallbackColors[Random.Default.nextInt(fallbackColors.size)]
}

fun Palette?.colorOrFallback(isDarkTheme: Boolean): Color {
    val fallback = if (isDarkTheme) ThemeColor.AlmostBlack.toArgb() else ThemeColor.White.toArgb()
    val dominantBlended = this?.dominantSwatch?.rgb?.let {
        ColorUtils.blendARGB(it, fallback, 0.25F)
    }
    val paletteColor = if (isDarkTheme) {
        this?.darkVibrantSwatch?.rgb ?:
        this?.darkMutedSwatch?.rgb ?:
        this?.mutedSwatch?.rgb ?:
        dominantBlended ?:
        fallback
    } else {
        this?.lightVibrantSwatch?.rgb ?:
        this?.lightMutedSwatch?.rgb ?:
        this?.mutedSwatch?.rgb ?:
        dominantBlended ?:
        fallback
    }
    return Color(paletteColor)
}
