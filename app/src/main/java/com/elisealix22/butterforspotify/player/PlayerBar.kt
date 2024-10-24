package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.music.AsyncAlbumImage
import com.elisealix22.butterforspotify.ui.UiMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.text
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.TextStyleAlbumTitle
import com.elisealix22.butterforspotify.ui.theme.TextStyleArtistTitle
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

val PlayerBarHeight = 64.dp
private val PlayerBarImageSize = 48.dp
private val PlayerBarRoundedCorner = 4.dp

private data class SurfaceConfig(
    val roundedCornerShape: RoundedCornerShape,
    val shadowElevation: Dp
)

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    containerWidth: Dp = LocalConfiguration.current.screenWidthDp.dp,
    containerHeight: Dp = LocalConfiguration.current.screenHeightDp.dp,
    horizontalPadding: Dp = Dimen.PaddingOneAndAHalf,
    onExpandChange: (offset: Float) -> Unit = {}
) {
    val expandedOffset = remember { mutableFloatStateOf(0F) }
    val surfaceConfig by remember {
        derivedStateOf {
            val corner = (1F - expandedOffset.floatValue) * PlayerBarRoundedCorner.value
            SurfaceConfig(
                roundedCornerShape = RoundedCornerShape(topStart = corner.dp, topEnd = corner.dp),
                shadowElevation = if (expandedOffset.floatValue == 1F) 0.dp else 8.dp
            )
        }
    }
    Surface(
        modifier = modifier.expandablePlayerBar(
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            horizontalPadding = horizontalPadding
        ) { offset ->
            expandedOffset.floatValue = offset
            onExpandChange(offset)
        },
        shape = surfaceConfig.roundedCornerShape,
        shadowElevation = surfaceConfig.shadowElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PlayerBarHeight)
                .padding(horizontal = (PlayerBarHeight - PlayerBarImageSize) / 2)
        ) {
            when (playerUiState) {
                is UiState.Success -> PlayerContent(playerUiState.data)
                is UiState.Error -> Error(playerUiState.message, playerUiState.onTryAgain)
                is UiState.Loading, is UiState.Initial -> {
                    playerUiState.data.let {
                        if (it == null) Connecting() else PlayerContent(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PlayerContent(player: Player) {
    TrackInfo(player)
    PlayButton(player)
}

@Composable
private fun RowScope.PlayButton(player: Player) {
    val isPaused = player.playerState.isPaused
    IconButton(
        modifier = Modifier.align(Alignment.CenterVertically),
        onClick = {
            if (isPaused) {
                player.spotifyApis?.playerApi?.resume()
            } else {
                player.spotifyApis?.playerApi?.pause()
            }
        },
        enabled = player.spotifyApis != null,
        content = {
            if (isPaused) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_24),
                    contentDescription = stringResource(R.string.resume)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_pause_24),
                    contentDescription = stringResource(R.string.pause)
                )
            }
        }
    )
}

@Composable
private fun RowScope.TrackInfo(player: Player) {
    AsyncAlbumImage(
        modifier = Modifier.align(Alignment.CenterVertically),
        imageUri = player.playerState.track.imageUri,
        imagesApi = player.spotifyApis?.imagesApi,
        imageDimension = com.spotify.protocol.types.Image.Dimension.THUMBNAIL,
        size = PlayerBarImageSize,
        contentDescription = stringResource(
            R.string.album_art_content_description,
            player.playerState.track.name
        )
    )
    Column(
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .weight(1F)
            .padding(start = Dimen.PaddingHalf)
    ) {
        Text(
            text = player.playerState.track.name,
            style = TextStyleAlbumTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val artistNames = remember(player.playerState.track.artists) {
            player.playerState.track.artists.joinToString { it.name }
        }
        Text(
            text = artistNames,
            style = TextStyleArtistTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RowScope.Connecting() {
    Box(
        modifier = Modifier
            .size(PlayerBarImageSize)
            .align(Alignment.CenterVertically)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center),
            strokeWidth = 3.dp
        )
    }
    Text(
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(start = Dimen.PaddingHalf, end = Dimen.Padding),
        text = stringResource(R.string.connecting_to_spotify),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RowScope.Error(uiErrorMessage: UiMessage?, onTryAgain: (() -> Unit)?) {
    IconButton(
        modifier = Modifier
            .size(PlayerBarImageSize)
            .align(Alignment.CenterVertically),
        onClick = onTryAgain ?: {},
        colors = IconButtonDefaults.iconButtonColors().copy(
            contentColor = ThemeColor.Tangerine
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh_24),
            contentDescription = stringResource(R.string.ui_state_try_again)
        )
    }
    Text(
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(start = Dimen.PaddingHalf, end = Dimen.Padding),
        text = uiErrorMessage?.text() ?: stringResource(R.string.ui_state_error),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@ThemePreview
@Composable
fun PlayerBarSuccessPreview() {
    val uiState = UiState.Success(MockPlayerWithLongTrackTitle)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
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
    val uiState = UiState.Loading(MockPlayerWithCachedState)
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarErrorPreview() {
    val uiState = UiState.Error<Player>(data = null, onTryAgain = {})
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}

@ThemePreview
@Composable
fun PlayerBarErrorLongMessagePreview() {
    val uiState = UiState.Error<Player>(
        data = null,
        message = UiMessage.Message("Really long error message connecting to Spotify"),
        onTryAgain = {}
    )
    ButterForSpotifyTheme {
        Surface {
            PlayerBar(playerUiState = uiState)
        }
    }
}
