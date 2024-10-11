package com.elisealix22.butterforspotify.music

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri
import kotlin.random.Random

private const val TAG = "AlbumImage"
private val AlbumCornerSize = 2.dp

private val FallbackColors = listOf(
    ThemeColor.Tangerine,
    ThemeColor.Orange,
    ThemeColor.Citrus,
    ThemeColor.Blue,
    ThemeColor.Pink
)

private fun fallbackPainter(): Painter = ColorPainter(
    FallbackColors[Random.Default.nextInt(FallbackColors.size)]
)

private data class AsyncBitmap(
    val isError: Boolean,
    val bitmap: Bitmap?
) {
    companion object {
        val Initial = AsyncBitmap(
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
fun AsyncAlbumImage(
    modifier: Modifier = Modifier,
    imageUri: ImageUri?,
    imagesApi: ImagesApi?,
    imageDimension: Image.Dimension,
    contentDescription: String,
    size: Dp
) {
    val asyncBitmap = remember(imageUri) { mutableStateOf(AsyncBitmap.Initial) }
    if (imageUri != null && imagesApi != null && asyncBitmap.value.bitmap == null) {
        LaunchedEffect(imageUri, imagesApi) {
            Log.i(TAG, "Fetching bitmap: $imageUri")
            imagesApi.getImage(
                imageUri,
                imageDimension
            ).setResultCallback { bitmap ->
                asyncBitmap.value = AsyncBitmap(isError = false, bitmap)
            }.setErrorCallback {
                asyncBitmap.value = AsyncBitmap(isError = false, null)
            }
        }
    }
    val imagePainter = when {
        asyncBitmap.value.bitmap != null -> rememberAsyncImagePainter(asyncBitmap.value.bitmap)
        asyncBitmap.value.isError -> fallbackPainter()
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

@ThemePreview
@Composable
fun AsyncAlbumImagePreview() {
    ButterForSpotifyTheme {
        Surface(modifier = Modifier.padding(Dimen.Padding)) {
            AsyncAlbumImage(
                imagesApi = null,
                imageUri = ImageUri("uri://"),
                imageDimension = Image.Dimension.SMALL,
                contentDescription = "Album",
                size = 48.dp
            )
        }
    }
}
