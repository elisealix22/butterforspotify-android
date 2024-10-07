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
import com.elisealix22.butterforspotify.data.playlist.FeaturedPlaylists
import com.elisealix22.butterforspotify.data.playlist.Playlist
import com.elisealix22.butterforspotify.data.playlist.Playlists
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.UiStateScaffold
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MusicScreen(
    viewModel: MusicViewModel = viewModel()
) {
    LifecycleStartEffect(Unit) {
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
    uiState: UiState<FeaturedPlaylists>,
    lazyListState: LazyListState
) {
    UiStateScaffold(
        uiState = uiState
    ) {
        MusicContent(
            playlists = uiState.data?.playlists?.items.orEmpty(),
            lazyListState = lazyListState
        )
    }
}

@Composable
private fun MusicContent(
    modifier: Modifier = Modifier,
    playlists: List<Playlist>,
    lazyListState: LazyListState
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState
    ) {
        items(playlists) { playlist ->
            Column {
                Text(
                    text = playlist.name
                )
                Text(
                    modifier = modifier.padding(bottom = Dimen.Padding),
                    text = playlist.description.orEmpty()
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun MusicScreenPreview() {
    val uiState = UiState.Success(
        data = FeaturedPlaylists(
            "message",
            playlists = Playlists(
                limit = 20,
                href = "href",
                next = null,
                offset = 0,
                total = 2,
                items = listOf(
                    Playlist(id = "123", href = "href", name = "list name 1", "list description 1"),
                    Playlist(id = "123", href = "href", name = "list name 1", "list description 1")
                )
            )
        )
    )
    ButterForSpotifyTheme {
        MusicUiScaffold(
            uiState = uiState,
            lazyListState = rememberLazyListState()
        )
    }
}
