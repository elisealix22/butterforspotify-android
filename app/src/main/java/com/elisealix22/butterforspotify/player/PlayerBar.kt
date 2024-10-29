package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.music.AsyncAlbumImage
import com.elisealix22.butterforspotify.ui.UiMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.isError
import com.elisealix22.butterforspotify.ui.text
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.LandscapeThemePreview
import com.elisealix22.butterforspotify.ui.theme.TextStyleAlbumTitle
import com.elisealix22.butterforspotify.ui.theme.TextStyleArtistTitle
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview
import com.spotify.protocol.types.Image

val PlayerBarHeight = 64.dp
private val TopAppBarHeight = 64.dp
private val PlayerBarImageSizeCollapsed = 48.dp
private val PlayerBarRoundedCorner = 4.dp

private data class SurfaceConfig(
    val roundedCornerShape: RoundedCornerShape,
    val shadowElevation: Dp
)

private data class ExpandedImageConfig(
    val isLandscape: Boolean,
    val expandedImageSize: Dp,
    val expandedImagePadding: PaddingValues,
    val expandedImageX: Dp,
    val expandedImageY: Dp
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
    val expandState = rememberSaveable { mutableStateOf(PlayerBarExpandState.Collapsed) }
    val expandOffset = remember { mutableFloatStateOf(0F) }
    val surfaceConfig by remember {
        derivedStateOf {
            val corner = (1F - expandOffset.floatValue) * PlayerBarRoundedCorner.value
            SurfaceConfig(
                roundedCornerShape = RoundedCornerShape(topStart = corner.dp, topEnd = corner.dp),
                shadowElevation = if (expandOffset.floatValue == 1F) 0.dp else 8.dp
            )
        }
    }
    val expandedImageConfig = remember(containerWidth, containerHeight) {
        expandedImageConfig(containerWidth, containerHeight)
    }
    LaunchedEffect(playerUiState) {
        if (expandState.value == PlayerBarExpandState.Expanded && playerUiState.isError()) {
            expandState.value = PlayerBarExpandState.Collapsed
        }
    }
    Surface(
        modifier = modifier
            .expandablePlayerBar(
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                horizontalPadding = horizontalPadding,
                enabled = playerUiState is UiState.Success,
                expandState = expandState
            ) { offset ->
                expandOffset.floatValue = offset
                onExpandChange(offset)
            },
        shape = surfaceConfig.roundedCornerShape,
        shadowElevation = surfaceConfig.shadowElevation
    ) {
        Box {
            CollapsedPlayerBar(
                modifier = Modifier.align(Alignment.BottomStart),
                playerUiState = playerUiState,
                expandedImageConfig = expandedImageConfig,
                expandOffset = expandOffset.floatValue
            )
            if (expandOffset.floatValue > 0F) {
                ExpandedPlayerBar(
                    modifier = Modifier.align(Alignment.TopStart),
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
    expandOffset: Float
) {
    val collapsedImagePadding = PlayerBarHeight.minus(PlayerBarImageSizeCollapsed).div(2)
    Box(modifier = modifier) {
        when (playerUiState) {
            is UiState.Success -> CollapsedPlayerContent(
                player = playerUiState.data,
                expandOffset = expandOffset,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageConfig = expandedImageConfig
            )
            is UiState.Loading,
            is UiState.Initial -> CollapsedLoadingContent(
                player = playerUiState.data,
                expandOffset = expandOffset,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageConfig = expandedImageConfig
            )
            is UiState.Error -> CollapsedErrorContent(
                uiErrorMessage = playerUiState.message,
                onTryAgain = playerUiState.onTryAgain,
                collapsedImagePadding = collapsedImagePadding
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedPlayerBar(
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
            expandedHeight = TopAppBarHeight,
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
            PlayButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                player = player
            )
            TrackInfo(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                player = player
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
    collapsedImagePadding: Dp
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
                    val offsetX = expandedImageConfig.expandedImageX.plus(collapsedImagePadding)
                    val offsetY = expandedImageConfig.expandedImageY.plus(collapsedImagePadding)
                    IntOffset(
                        x = (offsetX * expandOffset).roundToPx(),
                        y = (offsetY * expandOffset).roundToPx()
                    )
                },
            imageUri = player.playerState.track.imageUri,
            imagesApi = player.spotifyApis?.imagesApi,
            imageDimension = com.spotify.protocol.types.Image.Dimension.THUMBNAIL,
            size = PlayerBarImageSizeCollapsed,
            contentDescription = stringResource(
                R.string.track_art_content_description,
                player.playerState.track.name
            )
        )
        if (rowAlpha > 0F) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PlayerBarHeight)
                    .align(Alignment.BottomStart)
                    .alpha(rowAlpha)
                    .padding(
                        start = PlayerBarImageSizeCollapsed
                            .plus(collapsedImagePadding.times(2))
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
    collapsedImagePadding: Dp,
    expandedImageConfig: ExpandedImageConfig,
    expandOffset: Float,
    player: Player?
) {
    player.let {
        if (it == null) {
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
                        .padding(horizontal = Dimen.PaddingHalf),
                    text = stringResource(R.string.connecting_to_spotify),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            CollapsedPlayerContent(
                player = it,
                expandOffset = expandOffset,
                expandedImageConfig = expandedImageConfig,
                collapsedImagePadding = collapsedImagePadding
            )
        }
    }
}

@Composable
private fun CollapsedErrorContent(
    modifier: Modifier = Modifier,
    collapsedImagePadding: Dp,
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
                painter = painterResource(R.drawable.ic_refresh_24),
                contentDescription = stringResource(R.string.ui_state_try_again)
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = Dimen.PaddingHalf),
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

private fun expandedImageConfig(containerWidth: Dp, containerHeight: Dp): ExpandedImageConfig {
    val isLandscape = containerWidth > containerHeight
    val expandedImagePadding = PaddingValues(
        start = Dimen.Padding,
        end = Dimen.Padding,
        bottom = Dimen.Padding,
        top = TopAppBarHeight
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
