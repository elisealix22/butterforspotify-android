package com.elisealix22.butterforspotify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Color.Yellow,
    onPrimary = Color.AlmostBlack,
    secondary = Color.Tangerine,
    tertiary = Color.Pink,
    background = Color.AlmostBlack,
    surface = Color.AlmostBlack
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Yellow,
    onPrimary = Color.White,
    secondary = Color.Tangerine,
    tertiary = Color.Pink,
    background = Color.White,
    surface = Color.White
)

@Composable
fun ButterForSpotifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
