package com.elisealix22.butterforspotify.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elisealix22.butterforspotify.data.model.album.Album
import com.elisealix22.butterforspotify.data.model.album.AlbumType
import com.elisealix22.butterforspotify.data.model.track.TopTracksResponse
import com.elisealix22.butterforspotify.data.model.track.Track
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.UiStateScaffold
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MusicScreen(
    viewModel: MusicViewModel = viewModel()
) {
    LifecycleStartEffect(viewModel) {
        viewModel.fetchFeaturedPlaylists()
        onStopOrDispose { }
    }
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    MusicUiScaffold(
        uiState = uiState,
        lazyListState = lazyListState
    )
}

@Composable
private fun MusicUiScaffold(
    uiState: UiState<TopTracksResponse>,
    lazyListState: LazyListState
) {
    UiStateScaffold(
        uiState = uiState
    ) {
        MusicContent(
            items = uiState.data?.items.orEmpty(),
            lazyListState = lazyListState
        )
    }
}

@Composable
private fun MusicContent(
    modifier: Modifier = Modifier,
    items: List<Track>,
    lazyListState: LazyListState
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState
    ) {
        items(items) { track ->
            Column {
                Text(
                    text = track.name
                )
                Text(
                    modifier = modifier.padding(bottom = Dimen.Padding),
                    text = track.album.albumType.value
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun MusicScreenPreview() {
    val uiState = UiState.Success(
        data = TopTracksResponse(
            items = listOf(
                Track(
                    id = "123",
                    name = "Track 1",
                    album = Album(id = "Album 1", albumType = AlbumType.ALBUM, name = "Album 1")
                ),
                Track(
                    id = "345",
                    name = "Track 2",
                    album = Album(id = "Album 2", albumType = AlbumType.SINGLE, name = "Album 2")
                )
            ),
            next = null,
            total = 2
        )
    )
    ButterForSpotifyTheme {
        MusicUiScaffold(
            uiState = uiState,
            lazyListState = rememberLazyListState()
        )
    }
}
