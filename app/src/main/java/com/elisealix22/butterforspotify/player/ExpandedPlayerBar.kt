package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.music.AsyncAlbumImage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.LandscapeThemePreview
import com.elisealix22.butterforspotify.ui.theme.TextStyleAlbumTitle
import com.elisealix22.butterforspotify.ui.theme.TextStyleArtistTitle
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import com.spotify.protocol.types.Image

private val PlayerTopAppBarHeight = 64.dp

data class ExpandedImageConfig(
    val isLandscape: Boolean,
    val expandedImageSize: Dp,
    val expandedImagePadding: PaddingValues,
    val expandedImageX: Dp,
    val expandedImageY: Dp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayerBar(
    modifier: Modifier = Modifier,
    expandOffset: Float,
    expandedImageConfig: ExpandedImageConfig,
    playerUiState: UiState<Player>,
    onCloseClick: () -> Unit = {}
) {
    val player = playerUiState.data ?: return
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(expandOffset)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.primary
            ),
            expandedHeight = PlayerTopAppBarHeight,
            navigationIcon = {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_x_24),
                        contentDescription = stringResource(R.string.close_fullscreen_player)
                    )
                }
            },
            title = {}
        )
        AsyncAlbumImage(
            modifier = Modifier
                .alpha(if (expandOffset == 1F) 1F else 0F)
                .align(Alignment.TopStart)
                .padding(expandedImageConfig.expandedImagePadding)
                .size(expandedImageConfig.expandedImageSize),
            imageUri = player.playerState.track?.imageUri,
            imagesApi = player.spotifyApis?.imagesApi,
            imageDimension = Image.Dimension.LARGE,
            size = expandedImageConfig.expandedImageSize,
            contentDescription = stringResource(
                R.string.track_art_content_description,
                player.playerState.track?.name ?: ""
            )
        )
        Column(
            modifier = if (expandedImageConfig.isLandscape) {
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(Dimen.Padding)
            } else {
                Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .padding(
                        top = expandedImageConfig.expandedImagePadding.calculateTopPadding()
                            .plus(expandedImageConfig.expandedImageSize)
                            .plus(Dimen.Padding),
                        bottom = Dimen.Padding,
                        start = Dimen.Padding,
                        end = Dimen.Padding
                    )
            }
        ) {
            TrackInfo(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                player = player
            )
            PlayerControls(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                player = player
            )
        }
    }
}

@Composable
private fun TrackInfo(
    modifier: Modifier = Modifier,
    player: Player
) {
    Column(
        modifier = modifier
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
private fun PlayerControls(
    modifier: Modifier = Modifier,
    player: Player
) {
    val isPaused = player.playerState.isPaused
    IconButton(
        modifier = modifier.size(PlayerBarHeight),
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

fun expandedImageConfig(containerWidth: Dp, containerHeight: Dp): ExpandedImageConfig {
    val isLandscape = containerWidth > containerHeight
    val expandedImagePadding = PaddingValues(
        start = Dimen.Padding,
        end = Dimen.Padding,
        bottom = Dimen.Padding,
        top = PlayerTopAppBarHeight
    )
    val expandedImageSize = if (isLandscape) {
        containerHeight.minus(
            expandedImagePadding.calculateTopPadding()
                .plus(expandedImagePadding.calculateBottomPadding())
        )
    } else {
        containerWidth.minus(
            expandedImagePadding.calculateLeftPadding(LayoutDirection.Ltr)
                .plus(expandedImagePadding.calculateRightPadding(LayoutDirection.Ltr))
        )
    }
    return ExpandedImageConfig(
        isLandscape = isLandscape,
        expandedImagePadding = expandedImagePadding,
        expandedImageSize = expandedImageSize,
        expandedImageX = if (isLandscape) {
            0.dp
        } else {
            containerWidth.div(2)
                .minus(expandedImageSize.div(2))
                .minus(expandedImagePadding.calculateLeftPadding(LayoutDirection.Ltr))
        },
        expandedImageY = containerHeight.times(-1)
            .plus(expandedImagePadding.calculateTopPadding())
            .plus(expandedImageSize)
    )
}

@ThemePreview
@Composable
fun ExpandedPlayerBarPreview() {
    val uiState = UiState.Success(MockPlayerWithLongTrackTitle)
    val containerWidth = LocalConfiguration.current.screenWidthDp.dp
    val containerHeight = LocalConfiguration.current.screenWidthDp.dp
    ButterForSpotifyTheme {
        Surface {
            ExpandedPlayerBar(
                playerUiState = uiState,
                expandOffset = 1F,
                expandedImageConfig = expandedImageConfig(
                    containerWidth = containerWidth,
                    containerHeight = containerHeight
                )
            )
        }
    }
}

@ThemePreview
@Composable
fun ExpandedPlayerBarCachedPreview() {
    val uiState = UiState.Loading(MockPlayerWithCachedState)
    val containerWidth = LocalConfiguration.current.screenWidthDp.dp
    val containerHeight = LocalConfiguration.current.screenWidthDp.dp
    ButterForSpotifyTheme {
        Surface {
            ExpandedPlayerBar(
                playerUiState = uiState,
                expandOffset = 1F,
                expandedImageConfig = expandedImageConfig(
                    containerWidth = containerWidth,
                    containerHeight = containerHeight
                )
            )
        }
    }
}

@LandscapeThemePreview
@Composable
fun ExpandedPlayerBarLandscapePreview() {
    val uiState = UiState.Success(MockPlayerWithLongTrackTitle)
    val containerWidth = LocalConfiguration.current.screenWidthDp.dp
    val containerHeight = LocalConfiguration.current.screenWidthDp.dp
    ButterForSpotifyTheme {
        Surface {
            ExpandedPlayerBar(
                playerUiState = uiState,
                expandOffset = 1F,
                expandedImageConfig = expandedImageConfig(
                    containerWidth = containerWidth,
                    containerHeight = containerHeight
                )
            )
        }
    }
}
