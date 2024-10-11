package com.elisealix22.butterforspotify.music

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import kotlin.random.Random

private val FallbackColors = listOf(
    ThemeColor.Tangerine,
    ThemeColor.Orange,
    ThemeColor.Citrus,
    ThemeColor.Blue,
    ThemeColor.Pink
)

private val AlbumCornerSize = 2.dp

data class AlbumBitmap(
    val isError: Boolean,
    val bitmap: Bitmap?
) {
    companion object {
        val Placeholder = AlbumBitmap(
            isError = false,
            bitmap = null
        )
    }
}

@Composable
fun AlbumImage(
    modifier: Modifier = Modifier,
    url: String?,
    contentDescription: String,
    size: Dp
) {
    val fallback = fallbackPainter()
    AsyncImage(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AlbumCornerSize)),
        model = url,
        contentDescription = contentDescription,
        error = fallback
    )
}

@Composable
fun AlbumImage(
    modifier: Modifier = Modifier,
    albumBitmap: AlbumBitmap,
    contentDescription: String,
    size: Dp
) {
    val imagePainter = when {
        albumBitmap.bitmap != null -> rememberAsyncImagePainter(albumBitmap.bitmap)
        albumBitmap.isError -> fallbackPainter()
        else -> ColorPainter(Color.Transparent)
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AlbumCornerSize)),
        painter = imagePainter,
        contentDescription = contentDescription
    )
}

private fun fallbackPainter(): Painter
    = ColorPainter(FallbackColors[Random.Default.nextInt(FallbackColors.size)])

@ThemePreview
@Composable
fun AlbumImagePreview() {
    ButterForSpotifyTheme {
        Surface(modifier = Modifier.padding(Dimen.Padding)) {
            AlbumImage(
                url = "https://",
                contentDescription = "Album",
                size = 48.dp
            )
        }
    }
}
