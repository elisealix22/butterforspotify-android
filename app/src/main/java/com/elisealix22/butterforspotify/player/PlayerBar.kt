package com.elisealix22.butterforspotify.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.spotify.protocol.types.Image

val PlayerBarHeight = 64.dp
private val PlayerBarImageSizeCollapsed = 48.dp
private val PlayerBarImageSizeExpanded = 230.dp
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
    val expandedImageTopPadding = containerHeight / 4
    Surface(
        modifier = modifier
            .expandablePlayerBar(
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                horizontalPadding = horizontalPadding,
                enabled = playerUiState is UiState.Success
            ) { offset ->
                expandedOffset.floatValue = offset
                onExpandChange(offset)
            },
        shape = surfaceConfig.roundedCornerShape,
        shadowElevation = surfaceConfig.shadowElevation
    ) {
        Box {
            CollapsedPlayerBar(
                modifier = Modifier.align(Alignment.BottomStart),
                playerUiState = playerUiState,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                expandedImageTopPadding = expandedImageTopPadding,
                expandedOffset = expandedOffset.floatValue
            )
            ExpandedPlayerBar(
                modifier = Modifier.align(Alignment.TopCenter),
                playerUiState = playerUiState,
                expandedOffset = expandedOffset.floatValue,
                expandedImageTopPadding = expandedImageTopPadding,
            )
        }
    }
}

@Composable
private fun CollapsedPlayerBar(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    containerWidth: Dp,
    containerHeight: Dp,
    expandedImageTopPadding: Dp,
    expandedOffset: Float
) {
    val collapsedImagePadding = PlayerBarHeight.minus(PlayerBarImageSizeCollapsed).div(2)
    Box(modifier = modifier) {
        when (playerUiState) {
            is UiState.Success -> CollapsedPlayerContent(
                player = playerUiState.data,
                expandedOffset = expandedOffset,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageTopPadding = expandedImageTopPadding
            )
            is UiState.Loading,
            is UiState.Initial -> CollapsedLoadingContent(
                player = playerUiState.data,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                collapsedImagePadding = collapsedImagePadding,
                expandedImageTopPadding = expandedImageTopPadding
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
private fun ExpandedPlayerBar(
    modifier: Modifier = Modifier,
    expandedOffset: Float,
    expandedImageTopPadding: Dp,
    playerUiState: UiState<Player>
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(expandedOffset)
    ) {
        AsyncAlbumImage(
            modifier = Modifier
                .alpha(if (expandedOffset == 1F) 1F else 0F)
                .align(Alignment.TopCenter)
                .padding(top = expandedImageTopPadding)
                .size(PlayerBarImageSizeExpanded),
            imageUri = playerUiState.data?.playerState?.track?.imageUri,
            imagesApi = playerUiState.data?.spotifyApis?.imagesApi,
            imageDimension = Image.Dimension.THUMBNAIL,
            size = PlayerBarImageSizeExpanded,
            contentDescription = stringResource(
                R.string.album_art_content_description,
                playerUiState.data?.playerState?.track?.name ?: ""
            )
        )
    }
}

@Composable
private fun CollapsedPlayerContent(
    modifier: Modifier = Modifier,
    player: Player,
    expandedOffset: Float,
    containerWidth: Dp,
    containerHeight: Dp,
    expandedImageTopPadding: Dp,
    collapsedImagePadding: Dp
) {
    Box(modifier = modifier) {
        val rowAlpha = 1F - (expandedOffset / .05F).coerceIn(0F, 1F)
        val imageSize = PlayerBarImageSizeCollapsed.plus(
            PlayerBarImageSizeExpanded
                .minus(PlayerBarImageSizeCollapsed)
                .times(expandedOffset)
        )
        AsyncAlbumImage(
            modifier = Modifier
                .padding(collapsedImagePadding)
                .size(imageSize)
                .offset {
                    val endX = containerWidth.div(2)
                        .minus(PlayerBarImageSizeExpanded.div(2))
                        .minus(collapsedImagePadding)
                    val endY = containerHeight.times(-1)
                        .plus(expandedImageTopPadding)
                        .plus(PlayerBarImageSizeExpanded)
                        .plus(collapsedImagePadding)
                    IntOffset(
                        x = (endX * expandedOffset).roundToPx(),
                        y = (endY * expandedOffset).roundToPx()
                    )
                },
            imageUri = player.playerState.track.imageUri,
            imagesApi = player.spotifyApis?.imagesApi,
            imageDimension = com.spotify.protocol.types.Image.Dimension.THUMBNAIL,
            size = PlayerBarImageSizeCollapsed,
            contentDescription = stringResource(
                R.string.album_art_content_description,
                player.playerState.track.name
            )
        )
        if (rowAlpha > 0F) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PlayerBarHeight)
                    .alpha(rowAlpha)
                    .padding(
                        start = PlayerBarImageSizeCollapsed
                            .plus(collapsedImagePadding.times(2))
                    )
            ) {
                TrackInfo(player = player)
                PlayButton(player = player)
            }
        }
    }
}

@Composable
private fun CollapsedLoadingContent(
    modifier: Modifier = Modifier,
    containerWidth: Dp,
    containerHeight: Dp,
    collapsedImagePadding: Dp,
    expandedImageTopPadding: Dp,
    player: Player?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PlayerBarHeight)
    ) {
        player.let {
            if (it == null) {
                Box(
                    modifier = Modifier
                        .padding(start = collapsedImagePadding)
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
                        .padding(start = collapsedImagePadding, end = Dimen.Padding),
                    text = stringResource(R.string.connecting_to_spotify),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                CollapsedPlayerContent(
                    player = it,
                    expandedOffset = 0F,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight,
                    expandedImageTopPadding = expandedImageTopPadding,
                    collapsedImagePadding = collapsedImagePadding
                )
            }
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
            .height(PlayerBarHeight)
            .padding(start = collapsedImagePadding)
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
                .padding(start = Dimen.PaddingHalf, end = Dimen.Padding),
            text = uiErrorMessage?.text() ?: stringResource(R.string.ui_state_error),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RowScope.PlayButton(
    modifier: Modifier = Modifier,
    player: Player
) {
    val isPaused = player.playerState.isPaused
    IconButton(
        modifier = modifier.align(Alignment.CenterVertically),
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
private fun RowScope.TrackInfo(
    modifier: Modifier = Modifier,
    player: Player
) {
    Column(
        modifier = modifier
            .align(Alignment.CenterVertically)
            .weight(1F)
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
