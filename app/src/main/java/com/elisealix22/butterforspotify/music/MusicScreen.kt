package com.elisealix22.butterforspotify.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elisealix22.butterforspotify.data.playlist.Playlist
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MusicScreen(
    viewModel: MusicViewModel = viewModel()
) {
    val musicUiState = viewModel.uiState.collectAsState().value
    val playlists = musicUiState.featuredPlaylists?.playlists?.items.orEmpty()
    Surface {
        PlaylistList(playlists = playlists)
    }
}

@Composable
private fun PlaylistList(
    modifier: Modifier = Modifier,
    playlists: List<Playlist>
) {
    LazyColumn(modifier = modifier) {
        items(playlists) { playlist ->
            Column {
                Text(
                    text = playlist.name
                )
                // TODO(elise): Handle empty description
                Text(
                    text = playlist.description.orEmpty()
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun SignInScreenPreview() {
    ButterForSpotifyTheme {
        MusicScreen()
    }
}
