package com.elisealix22.butterforspotify.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.playlist.FeaturedPlaylists
import com.elisealix22.butterforspotify.data.playlist.PlaylistService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val playlistService = PlaylistService()

    data class MusicUiState(
        val title: String,
        val featuredPlaylists: FeaturedPlaylists? = null
    )

    private val _uiState = MutableStateFlow(MusicUiState("Hello Music tab!!"))
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    init {
        fetchFeaturedPlaylists()
    }

    private fun fetchFeaturedPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistService.fetchFeaturedPlaylists()
                .onStart {

                }
                .catch { error ->
                    // TODO(elise): show error
                    Log.e("###", "ERROR", error)
                }
                .collect { featuredPlaylists ->
                    _uiState.value = MusicUiState("New title", featuredPlaylists)
                }
        }
    }
}
