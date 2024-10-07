package com.elisealix22.butterforspotify.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.playlist.FeaturedPlaylists
import com.elisealix22.butterforspotify.data.playlist.PlaylistService
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.toUiErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val playlistService = PlaylistService()

    private val _uiState = MutableStateFlow<UiState<FeaturedPlaylists>>(UiState.Loading(null))
    val uiState: StateFlow<UiState<FeaturedPlaylists>> = _uiState.asStateFlow()

    fun fetchFeaturedPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistService.fetchFeaturedPlaylists()
                .onStart {
                    _uiState.value = UiState.Loading(data = _uiState.value.data)
                }
                .catch { error ->
                    _uiState.value = UiState.Error(
                        message = error.toUiErrorMessage(),
                        data = _uiState.value.data
                    )
                }
                .collect { featuredPlaylists ->
                    _uiState.value = UiState.Success(featuredPlaylists)
                }
        }
    }
}
