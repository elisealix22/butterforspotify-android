package com.elisealix22.butterforspotify

import androidx.collection.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.spotify.protocol.types.Image.Dimension
import com.spotify.protocol.types.ImageUri

object PaletteCache {
    private const val MAX_PALETTES = 500
    private val lruCache = LruCache<String, Palette>(MAX_PALETTES)

    fun put(imageUri: ImageUri?, dimension: Dimension, palette: Palette): Palette? = imageUri?.let {
        put(imageUri.cacheKey(dimension), palette)
    }

    fun get(imageUri: ImageUri?, dimension: Dimension): Palette? = imageUri?.let {
        get(imageUri.cacheKey(dimension))
    }

    private fun put(key: String?, palette: Palette): Palette? {
        return key?.let { lruCache.put(key, palette) }
    }

    private fun get(key: String?): Palette? = key?.let { lruCache[it] }
}

fun ImageUri.cacheKey(dimension: Dimension) = "${this.raw}:${dimension.value}"

fun Palette?.colorOrFallback(isDarkTheme: Boolean): Color {
    val fallback = if (isDarkTheme) ThemeColor.AlmostBlack.toArgb() else ThemeColor.White.toArgb()
    val dominantBlended = this?.dominantSwatch?.rgb?.let {
        ColorUtils.blendARGB(it, fallback, 0.5F)
    }
    val paletteColor = if (isDarkTheme) {
        this?.darkVibrantSwatch?.rgb
            ?: this?.darkMutedSwatch?.rgb
            ?: this?.mutedSwatch?.rgb
            ?: dominantBlended
            ?: fallback
    } else {
        this?.lightVibrantSwatch?.rgb
            ?: this?.lightMutedSwatch?.rgb
            ?: this?.mutedSwatch?.rgb
            ?: dominantBlended
            ?: fallback
    }
    return Color(paletteColor)
}
