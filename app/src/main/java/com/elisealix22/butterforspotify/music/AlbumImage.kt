package com.elisealix22.butterforspotify.music

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import com.elisealix22.butterforspotify.PaletteCache
import com.elisealix22.butterforspotify.cacheKey
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.Image.Dimension
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
        model = url,
        contentDescription = contentDescription,
        error = errorPainter(),
        placeholder = placeholderPainter()
    )
}

@Composable
fun AsyncAlbumImage(
    modifier: Modifier = Modifier,
    imagesApi: ImagesApi?,
    imageUri: ImageUri?,
    imageDimension: Dimension,
    contentDescription: String,
    size: Dp,
    onPaletteLoaded: (palette: Palette?) -> Unit = {}
) {
    val context = LocalContext.current
    val image = remember(imageUri, imageDimension) {
        mutableStateOf(
            imageUri?.coilKey(imageDimension).let {
                if (it == null) null else context.imageLoader.memoryCache?.get(it)?.image
            }
        )
    }
    val isError = remember { mutableStateOf(false) }
    if (imageUri != null && imagesApi != null) {
        LaunchedEffect(imageUri, imagesApi) {
            val cachedImage = image.value
            val cachedPalette = PaletteCache.get(imageUri, imageDimension)
            if (cachedImage == null || cachedPalette == null) {
                Log.i(TAG, "Fetching bitmap: $imageUri")
                isError.value = false
                imagesApi.getImage(
                    imageUri,
                    imageDimension
                ).setResultCallback { newBitmap ->
                    val newPalette = Palette.from(newBitmap).generate()
                    PaletteCache.put(imageUri, imageDimension, newPalette)
                    onPaletteLoaded(newPalette)
                    val newImage = newBitmap.asImage()
                    context.imageLoader.memoryCache?.set(
                        imageUri.coilKey(imageDimension),
                        MemoryCache.Value(newImage)
                    )
                    image.value = newImage
                }.setErrorCallback {
                    isError.value = true
                }
            } else {
                onPaletteLoaded(cachedPalette)
            }
        }
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AlbumCornerSize)),
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(image.value)
                .memoryCacheKey(imageUri?.coilKey(imageDimension))
                .build(),
            error = if (isError.value) errorPainter() else placeholderPainter(),
            placeholder = placeholderPainter()
        ),
        contentDescription = contentDescription
    )
}

private fun ImageUri.coilKey(imageDimension: Dimension): MemoryCache.Key =
    MemoryCache.Key(this.cacheKey(imageDimension))

@Composable
private fun errorPainter(): Painter = ColorPainter(
    FallbackColors[Random.Default.nextInt(FallbackColors.size)]
)

@Composable
private fun placeholderPainter(): Painter = ColorPainter(Color.Transparent)

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
