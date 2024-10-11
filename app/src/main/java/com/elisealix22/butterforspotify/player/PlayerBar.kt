package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.music.AsyncAlbumImage
import com.elisealix22.butterforspotify.ui.Player1
import com.elisealix22.butterforspotify.ui.UiErrorMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.text
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import com.spotify.protocol.types.Image

private val PlayerBarImageSize = 48.dp

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>
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
                is UiState.Success -> TrackInfo(playerUiState.data)
                is UiState.Error -> Error(playerUiState.message)
                is UiState.Loading, is UiState.Initial -> {
                    playerUiState.data.let {
                        if (it == null) Connecting() else TrackInfo(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TrackInfo(
    player: Player
) {
    AsyncAlbumImage(
        modifier = Modifier
            .padding(end = Dimen.PaddingHalf)
            .align(Alignment.CenterVertically),
        imageUri = player.playerState.track.imageUri,
        imagesApi = player.spotifyApis?.imagesApi,
        imageDimension = Image.Dimension.THUMBNAIL,
        size = PlayerBarImageSize,
        contentDescription = stringResource(
            R.string.album_art_content_description,
            player.playerState.track.name
        )
    )
    Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = player.playerState.track.name
    )
}

@Composable
private fun RowScope.Connecting() {
    Spacer(
        modifier = Modifier
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
    val uiState = UiState.Loading<Player>(null)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarLoadingWithContentPreview() {
    val uiState = UiState.Loading(Player1)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarErrorPreview() {
    val uiState = UiState.Error<Player>(null)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarSuccessPreview() {
    val uiState = UiState.Success(Player1)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}
