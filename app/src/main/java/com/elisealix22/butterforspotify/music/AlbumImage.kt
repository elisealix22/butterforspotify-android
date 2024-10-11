package com.elisealix22.butterforspotify.music

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.crossfade
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

@Composable
fun AlbumImage(
    modifier: Modifier = Modifier,
    url: String?,
    contentDescription: String,
    size: Dp
) {
    AsyncImage(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AlbumCornerSize)),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        error = fallbackPainter(),
        placeholder = placeholderPainter()
    )
}

@Composable
fun AsyncAlbumImage(
    modifier: Modifier = Modifier,
    imagesApi: ImagesApi?,
    imageUri: ImageUri?,
    imageDimension: Image.Dimension,
    contentDescription: String,
    size: Dp
) {
    val context = LocalContext.current
    val cacheKey = MemoryCache.Key("$imageUri:${imageDimension.value}")
    val image = remember(imageUri, imageDimension) {
        mutableStateOf(context.imageLoader.memoryCache?.get(cacheKey)?.image)
    }
    val isError = remember { mutableStateOf(false) }
    if (imageUri != null && imagesApi != null && image.value == null) {
        LaunchedEffect(imageUri, imagesApi) {
            Log.i(TAG, "Fetching bitmap: $imageUri")
            isError.value = false
            imagesApi.getImage(
                imageUri,
                imageDimension
            ).setResultCallback { newBitmap ->
                val newImage = newBitmap.asImage()
                context.imageLoader.memoryCache?.set(cacheKey, MemoryCache.Value(newImage))
                image.value = newImage
            }.setErrorCallback {
                isError.value = true
            }
        }
    }
    val imagePainter = when {
        image.value != null -> rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(image.value)
                .memoryCacheKey(cacheKey)
                .build()
        )
        isError.value -> fallbackPainter()
        else -> placeholderPainter()
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AlbumCornerSize)),
        painter = imagePainter,
        contentDescription = contentDescription
    )
}

@Composable
private fun fallbackPainter(): Painter = ColorPainter(
    FallbackColors[Random.Default.nextInt(FallbackColors.size)]
)

@Composable
private fun placeholderPainter(): Painter = ColorPainter(
    MaterialTheme.colorScheme.onSurface
)

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
