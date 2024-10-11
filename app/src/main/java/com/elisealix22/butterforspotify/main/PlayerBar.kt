package com.elisealix22.butterforspotify.main

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.music.AlbumBitmap
import com.elisealix22.butterforspotify.music.AlbumImage
import com.elisealix22.butterforspotify.ui.PlayerState1
import com.elisealix22.butterforspotify.ui.UiErrorMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.text
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState

val PlayerBarImageSize = 48.dp

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<PlayerState>,
    spotifyApis: PlayerViewModel.SpotifyApis? = null
) {
    Surface(
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.Padding, vertical = Dimen.PaddingHalf)
        ) {
            when (playerUiState) {
                is UiState.Success -> TrackInfo(playerUiState.data, spotifyApis)
                is UiState.Error -> Error(playerUiState.message)
                is UiState.Loading, is UiState.Initial -> Connecting()
            }
        }
    }
}

@Composable
private fun RowScope.TrackInfo(
    playerState: PlayerState,
    spotifyApis: PlayerViewModel.SpotifyApis?
) {
    val imageUri = playerState.track.imageUri
    val imageBitmap = remember(imageUri) { mutableStateOf(AlbumBitmap.Placeholder) }
    LaunchedEffect(imageUri) {
        // TODO(elise): get palette
        Log.e("####", "IMAGE URI: $imageUri APIS: $spotifyApis")
        if (imageUri != null && spotifyApis != null) {
            Log.e("####", "LOADING IMAGE: $imageUri")
            spotifyApis.imagesApi.getImage(
                imageUri,
                Image.Dimension.THUMBNAIL
            ).setResultCallback { bitmap ->
                imageBitmap.value = AlbumBitmap(isError = false, bitmap)
            }.setErrorCallback {
                imageBitmap.value = AlbumBitmap(isError = true, null)
            }
        }
    }
    AlbumImage(
        modifier = Modifier
            .padding(end = Dimen.PaddingHalf)
            .align(Alignment.CenterVertically),
        albumBitmap = imageBitmap.value,
        size = PlayerBarImageSize,
        contentDescription = stringResource(
            R.string.album_art_content_description,
            playerState.track.name
        )
    )
    Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = playerState.track.name
    )
}

@Composable
private fun RowScope.Connecting() {
    Spacer(
        modifier = Modifier
            .padding(end = Dimen.PaddingHalf)
            .size(PlayerBarImageSize)
            .align(Alignment.CenterVertically)
    )
    Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = stringResource(R.string.connecting_to_spotify),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RowScope.Error(uiErrorMessage: UiErrorMessage?) {
    Spacer(
        modifier = Modifier
            .padding(end = Dimen.PaddingHalf)
            .size(PlayerBarImageSize)
            .align(Alignment.CenterVertically)
    )
    Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = uiErrorMessage.text(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@ThemePreview
@Composable
fun PlayerBarLoadingEmptyPreview() {
    val uiState = UiState.Loading<PlayerState>(null)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarLoadingWithContentPreview() {
    val uiState = UiState.Loading(PlayerState1)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarErrorPreview() {
    val uiState = UiState.Error<PlayerState>(null)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarSuccessPreview() {
    val uiState = UiState.Success(PlayerState1)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}
