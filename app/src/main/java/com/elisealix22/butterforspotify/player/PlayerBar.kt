package com.elisealix22.butterforspotify.player

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.elisealix22.butterforspotify.PaletteCache
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.colorOrFallback
import com.elisealix22.butterforspotify.music.AsyncAlbumImage
import com.elisealix22.butterforspotify.ui.UiMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.isError
import com.elisealix22.butterforspotify.ui.text
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.TextStyleAlbumTitle
import com.elisealix22.butterforspotify.ui.theme.TextStyleArtistTitle
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

private val PlayerBarHeight = 64.dp
private val PlayerBarImageSizeCollapsed = 48.dp
private val PlayerBarRoundedCorner = 8.dp
private val PlayerTextStartPadding = 12.dp
private val CollapsedImageDimension = com.spotify.protocol.types.Image.Dimension.SMALL

@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    containerWidth: Dp = LocalConfiguration.current.screenWidthDp.dp,
    containerHeight: Dp = LocalConfiguration.current.screenHeightDp.dp,
    horizontalPadding: Dp = Dimen.PaddingOneAndAHalf,
    bottomPadding: Dp = 0.dp,
    onExpandChange: (offset: Float) -> Unit = {}
) {
    val expandState = rememberSaveable { mutableStateOf(PlayerBarExpandState.Collapsed) }
    val expandOffset = remember { mutableFloatStateOf(expandState.value.initialOffset()) }
    val expandedImageConfig = expandedImageConfig(containerWidth, containerHeight)
    val collapsedHeight = PlayerBarHeight.plus(bottomPadding)
    val collapsedImagePadding = PlayerBarHeight.minus(PlayerBarImageSizeCollapsed).div(2).let {
        PaddingValues(start = it, top = it, end = it, bottom = it.plus(bottomPadding))
    }
    val track by remember(playerUiState) {
        mutableStateOf(playerUiState.data?.playerState?.track)
    }
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val paletteColor = remember {
        mutableStateOf(
            PaletteCache.get(track?.imageUri, CollapsedImageDimension).colorOrFallback(isDarkTheme)
        )
    }
    val playerBarColor = animateColorAsState(
        targetValue = if (track != null) paletteColor.value else surfaceColor,
        label = "PlayerBarColor"
    )

    LaunchedEffect(playerUiState) {
        if (expandState.value == PlayerBarExpandState.Expanded && playerUiState.isError()) {
            expandState.value = PlayerBarExpandState.Collapsed
        }
    }

    Surface(
        modifier = modifier
            .expandablePlayerBar(
                collapsedHeight = collapsedHeight,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                horizontalPadding = horizontalPadding,
                enabled = track != null || expandState.value == PlayerBarExpandState.Expanded,
                expandState = expandState.value
            ) { newOffset ->
                expandState.value = if (newOffset == 1F) {
                    PlayerBarExpandState.Expanded
                } else {
                    PlayerBarExpandState.Collapsed
                }
                expandOffset.floatValue = newOffset
                onExpandChange(newOffset)
            },
        color = playerBarColor.value,
        shape = PlayerBarRoundedCorner.value.times(1F.minus(expandOffset.floatValue)).let { dp ->
            RoundedCornerShape(topStart = dp, topEnd = dp)
        },
        shadowElevation = 8.dp
    ) {
        Box {
            CollapsedPlayerBar(
                modifier = Modifier
                    .heightIn(min = collapsedHeight)
                    .align(Alignment.BottomStart),
                playerUiState = playerUiState,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageConfig = expandedImageConfig,
                expandOffset = expandOffset.floatValue
            ) { palette ->
                paletteColor.value = palette.colorOrFallback(isDarkTheme)
            }
            if (expandOffset.floatValue > 0F) {
                ExpandedPlayerBar(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopStart),
                    playerUiState = playerUiState,
                    expandedImageConfig = expandedImageConfig,
                    expandOffset = expandOffset.floatValue,
                    onCloseClick = {
                        expandState.value = PlayerBarExpandState.Collapsed
                    }
                )
            }
        }
    }
}

@Composable
private fun CollapsedPlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    expandedImageConfig: ExpandedImageConfig,
    expandOffset: Float,
    collapsedImagePadding: PaddingValues,
    onPaletteLoaded: (palette: Palette?) -> Unit
) {
    Box(modifier = modifier) {
        when (playerUiState) {
            is UiState.Success -> CollapsedPlayerContent(
                player = playerUiState.data,
                expandOffset = expandOffset,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageConfig = expandedImageConfig,
                onPaletteLoaded = onPaletteLoaded
            )
            is UiState.Loading,
            is UiState.Initial -> CollapsedLoadingContent(
                player = playerUiState.data,
                expandOffset = expandOffset,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageConfig = expandedImageConfig,
                onPaletteLoaded = onPaletteLoaded
            )
            is UiState.Error -> CollapsedErrorContent(
                uiErrorMessage = playerUiState.message,
                onTryAgain = playerUiState.onTryAgain,
                collapsedImagePadding = collapsedImagePadding
            )
        }
    }
}

@Composable
private fun CollapsedPlayerContent(
    modifier: Modifier = Modifier,
    player: Player,
    expandOffset: Float,
    expandedImageConfig: ExpandedImageConfig,
    collapsedImagePadding: PaddingValues,
    onPaletteLoaded: (palette: Palette?) -> Unit
) {
    Box(modifier = modifier) {
        val rowAlpha = 1F - (expandOffset / .05F).coerceIn(0F, 1F)
        val imageSize = PlayerBarImageSizeCollapsed.plus(
            expandedImageConfig.expandedImageSize
                .minus(PlayerBarImageSizeCollapsed)
                .times(expandOffset)
        )
        AsyncAlbumImage(
            modifier = Modifier
                .padding(collapsedImagePadding)
                .size(imageSize)
                .offset {
                    val offsetX = expandedImageConfig.expandedImageX
                        .plus(collapsedImagePadding.calculateLeftPadding(LayoutDirection.Ltr))
                    val offsetY = expandedImageConfig.expandedImageY
                        .plus(collapsedImagePadding.calculateBottomPadding())
                    IntOffset(
                        x = (offsetX * expandOffset).roundToPx(),
                        y = (offsetY * expandOffset).roundToPx()
                    )
                },
            imageUri = player.playerState.track?.imageUri,
            imagesApi = player.spotifyApis?.imagesApi,
            imageDimension = CollapsedImageDimension,
            size = PlayerBarImageSizeCollapsed,
            contentDescription = stringResource(
                R.string.track_art_content_description,
                player.playerState.track?.name ?: ""
            ),
            onPaletteLoaded = onPaletteLoaded
        )
        if (rowAlpha > 0F) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        max = PlayerBarImageSizeCollapsed +
                            collapsedImagePadding.calculateTopPadding() +
                            collapsedImagePadding.calculateBottomPadding()
                    )
                    .align(Alignment.BottomStart)
                    .alpha(rowAlpha)
                    .padding(
                        top = collapsedImagePadding.calculateTopPadding(),
                        bottom = collapsedImagePadding.calculateBottomPadding(),
                        start = PlayerBarImageSizeCollapsed
                            .plus(collapsedImagePadding.calculateStartPadding(LayoutDirection.Ltr))
                            .plus(PlayerTextStartPadding)
                    )
            ) {
                TrackInfo(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1F),
                    player = player
                )
                PlayButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    player = player
                )
            }
        }
    }
}

@Composable
private fun CollapsedLoadingContent(
    modifier: Modifier = Modifier,
    collapsedImagePadding: PaddingValues,
    expandedImageConfig: ExpandedImageConfig,
    expandOffset: Float,
    player: Player?,
    onPaletteLoaded: (palette: Palette?) -> Unit
) {
    if (player == null) {
        if (expandOffset == 0F) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(collapsedImagePadding)
            ) {
                Box(
                    modifier = Modifier
                        .size(PlayerBarImageSizeCollapsed)
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
                        .padding(horizontal = PlayerTextStartPadding),
                    text = stringResource(R.string.connecting_to_spotify),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        CollapsedPlayerContent(
            player = player,
            expandOffset = expandOffset,
            expandedImageConfig = expandedImageConfig,
            collapsedImagePadding = collapsedImagePadding,
            onPaletteLoaded = onPaletteLoaded
        )
    }
}

@Composable
private fun CollapsedErrorContent(
    modifier: Modifier = Modifier,
    collapsedImagePadding: PaddingValues,
    uiErrorMessage: UiMessage?,
    onTryAgain: (() -> Unit)?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(collapsedImagePadding)
    ) {
        IconButton(
            modifier = Modifier
                .size(PlayerBarImageSizeCollapsed)
                .align(Alignment.CenterVertically),
            onClick = onTryAgain ?: {},
            colors = IconButtonDefaults.iconButtonColors().copy(
                contentColor = ThemeColor.Tangerine
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.ui_state_try_again)
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = PlayerTextStartPadding),
            text = uiErrorMessage?.text() ?: stringResource(R.string.ui_state_error),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayButton(
    modifier: Modifier = Modifier,
    player: Player
) {
    val isPaused = player.playerState.isPaused
    val haptic = LocalHapticFeedback.current
    IconButton(
        modifier = modifier.size(PlayerBarHeight),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
private fun TrackInfo(
    modifier: Modifier = Modifier,
    player: Player
) {
    val track = player.playerState.track ?: return
    Column(
        modifier = modifier
    ) {
        Text(
            text = track.name,
            style = TextStyleAlbumTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = remember(track.artists) { track.artists.joinToString { it.name } },
            style = TextStyleArtistTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlayerBarSpacer() = Spacer(modifier = Modifier.height(PlayerBarHeight))

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
