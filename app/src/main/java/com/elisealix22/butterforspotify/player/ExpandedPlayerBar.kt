package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Box(modifier = modifier) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.primary
            ),
            expandedHeight = PlayerTopAppBarHeight,
            navigationIcon = {
                IconButton(
                    modifier = Modifier
                        .alpha((expandOffset - 0.9F).coerceIn(0F, 1F).div(0.1F)),
                    onClick = onCloseClick
                ) {
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
        val contentAlpha = (expandOffset - 0.75F).coerceIn(0F, 1F).div(0.25F)
        if (expandedImageConfig.isLandscape) {
            LandscapeContent(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .windowInsetsPadding(WindowInsets.systemBars),
                player = player,
                expandedImageConfig = expandedImageConfig
            )
        } else {
            PortraitContent(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .windowInsetsPadding(WindowInsets.systemBars),
                player = player,
                expandedImageConfig = expandedImageConfig
            )
        }
    }
}

@Composable
private fun LandscapeContent(
    modifier: Modifier = Modifier,
    player: Player,
    expandedImageConfig: ExpandedImageConfig
) {
    val padding = Dimen.PaddingDouble
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = padding,
                start = expandedImageConfig.expandedImageSize.plus(padding),
                end = padding
            )
    ) {
        Spacer(Modifier.weight(1F))
        TrackInfo(player = player)
        PlayerControls(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = padding),
            player = player
        )
    }
}

@Composable
private fun PortraitContent(
    modifier: Modifier = Modifier,
    player: Player,
    expandedImageConfig: ExpandedImageConfig
) {
    val padding = Dimen.PaddingDouble
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = expandedImageConfig.expandedImagePadding
                    .calculateTopPadding()
                    .plus(expandedImageConfig.expandedImageSize)
                    .plus(padding)
            )
    ) {
        TrackInfo(
            modifier = Modifier.padding(horizontal = padding),
            player = player
        )
        Spacer(modifier = Modifier.weight(1F))
        PlayerControls(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(start = padding, end = padding, bottom = padding),
            player = player
        )
    }
}

@Composable
private fun TrackInfo(
    modifier: Modifier = Modifier,
    player: Player,
    textAlign: TextAlign = TextAlign.Center
) {
    val track = player.playerState.track ?: return
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimen.PaddingHalf),
            text = track.name,
            style = TextStyleAlbumTitle.copy(fontSize = 24.sp, lineHeight = 32.sp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = remember(track.artists) { track.artists.joinToString { it.name } },
            style = TextStyleArtistTitle.copy(fontSize = 18.sp, lineHeight = 26.sp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign
        )
    }
}

@Composable
private fun PlayerControls(
    modifier: Modifier = Modifier,
    player: Player
) {
    val iconSize = 64.dp
    val haptic = LocalHapticFeedback.current
    val enabled = player.spotifyApis != null
    val isPaused = player.playerState.isPaused
    Row(modifier = modifier) {
        IconButton(
            modifier = Modifier.padding(Dimen.Padding),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                player.spotifyApis?.playerApi?.skipPrevious()
            },
            enabled = enabled,
            content = {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(R.drawable.ic_previous_24),
                    contentDescription = stringResource(R.string.previous)
                )
            }
        )
        IconButton(
            modifier = Modifier.padding(Dimen.Padding),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isPaused) {
                    player.spotifyApis?.playerApi?.resume()
                } else {
                    player.spotifyApis?.playerApi?.pause()
                }
            },
            enabled = enabled,
            content = {
                if (isPaused) {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_play_circle_24),
                        contentDescription = stringResource(R.string.resume)
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_pause_circle_24),
                        contentDescription = stringResource(R.string.pause)
                    )
                }
            }
        )
        IconButton(
            modifier = Modifier.padding(Dimen.Padding),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                player.spotifyApis?.playerApi?.skipNext()
            },
            enabled = enabled,
            content = {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(R.drawable.ic_next_24),
                    contentDescription = stringResource(R.string.next)
                )
            }
        )
    }
}

@Composable
fun expandedImageConfig(containerWidth: Dp, containerHeight: Dp): ExpandedImageConfig {
    val isLandscape = containerWidth > containerHeight
    val expandedImagePadding = PaddingValues(
        start = Dimen.Padding,
        end = Dimen.Padding,
        bottom = Dimen.Padding,
        top = PlayerTopAppBarHeight +
            WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
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
    val containerHeight = LocalConfiguration.current.screenHeightDp.dp
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
    val containerHeight = LocalConfiguration.current.screenHeightDp.dp
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
    val containerHeight = LocalConfiguration.current.screenHeightDp.dp
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
