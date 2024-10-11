package com.elisealix22.butterforspotify.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.model.SpotifyImage
import com.elisealix22.butterforspotify.data.model.album.Album
import com.elisealix22.butterforspotify.data.model.album.AlbumType
import com.elisealix22.butterforspotify.data.model.album.ReleaseDatePrecision
import com.elisealix22.butterforspotify.data.model.artist.Artist
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.ui.Player1
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.UiStateScaffold
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MusicScreen(
    viewModel: MusicViewModel = viewModel(),
    playerUiState: UiState<Player>
) {
    LifecycleStartEffect(viewModel) {
        viewModel.fetchFeaturedPlaylists()
        onStopOrDispose { }
    }
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    MusicUiScaffold(
        uiState = uiState,
        lazyListState = lazyListState,
        playerUiState = playerUiState
    )
}

@Composable
private fun MusicUiScaffold(
    uiState: UiState<List<Album>>,
    lazyListState: LazyListState,
    playerUiState: UiState<Player>
) {
    UiStateScaffold(
        uiState = uiState
    ) {
        MusicContent(
            items = uiState.data.orEmpty(),
            lazyListState = lazyListState,
            playerUiState = playerUiState
        )
    }
}

private data class ColumnConfig(
    val numColumns: Int,
    val columnSize: Dp
)

@Composable
private fun MusicContent(
    modifier: Modifier = Modifier,
    items: List<Album>,
    lazyListState: LazyListState,
    playerUiState: UiState<Player>
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val columnPadding = Dimen.Padding
    val columnConfig = remember(screenWidth, screenHeight) {
        val numColumns = if (screenWidth > screenHeight) 4 else 2
        ColumnConfig(
            numColumns = numColumns,
            columnSize = (screenWidth - (columnPadding * (numColumns + 1))) / numColumns
        )
    }
    val rowData = remember(items, columnConfig.numColumns) {
        items.chunked(columnConfig.numColumns)
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState
    ) {
        itemsIndexed(rowData) { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (index == 0) columnPadding else 0.dp,
                        bottom = if (index == rowData.lastIndex) {
                            Dimen.PaddingDouble
                        } else {
                            Dimen.PaddingOneAndAHalf
                        },
                        start = columnPadding
                    )
            ) {
                row.forEach { album ->
                    Column(
                        modifier = Modifier
                            .width(columnConfig.columnSize + columnPadding)
                            .padding(end = columnPadding)
                            .clickable(
                                onClickLabel = stringResource(R.string.play_x, album.name),
                                enabled = playerUiState is UiState.Success
                            ) {
                                playerUiState.data?.spotifyApis?.playerApi?.play(album.uri)
                            }
                    ) {
                        AlbumImage(
                            size = columnConfig.columnSize,
                            url = album.images.firstOrNull()?.url,
                            contentDescription = album.name
                        )
                        Text(
                            modifier = Modifier.padding(top = Dimen.PaddingHalf),
                            text = album.name,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val artistNames = remember(album.artists) {
                            album.artists.joinToString { it.name }
                        }
                        Text(
                            text = artistNames,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@ThemePreview
@Composable
fun MusicScreenPreview() {
    val albums = listOf(
        Album(
            id = "album1",
            albumType = AlbumType.ALBUM,
            name = "Album 1 with a really long name",
            images = listOf(
                SpotifyImage(
                    width = 640,
                    url = "https://i.scdn.co/image/ab67616d0000b27371d62ea7ea8a5be92d3c1f62",
                    height = 640
                )
            ),
            artists = listOf(
                Artist(id = "artist1", name = "Arist 1 with a really long name")
            ),
            uri = "uri://1",
            totalTracks = 3,
            releaseDate = "1989-06",
            releaseDatePrecision = ReleaseDatePrecision.MONTH
        ),
        Album(
            id = "Album 2",
            albumType = AlbumType.SINGLE,
            name = "Album 2",
            images = listOf(
                SpotifyImage(
                    width = 640,
                    url = "https://i.scdn.co/image/ab67616d0000b2738ef2562a1156ea6766e00ecb",
                    height = 640
                )
            ),
            artists = listOf(
                Artist(id = "artist1", name = "Arist 1"),
                Artist(id = "artist2", name = "Arist 2")
            ),
            uri = "uri://2",
            totalTracks = 10,
            releaseDate = "2021-10-01",
            releaseDatePrecision = ReleaseDatePrecision.DAY
        )
    )
    val uiState = UiState.Success(data = albums)
    ButterForSpotifyTheme {
        MusicUiScaffold(
            uiState = uiState,
            lazyListState = rememberLazyListState(),
            playerUiState = UiState.Success(Player1)
        )
    }
}
