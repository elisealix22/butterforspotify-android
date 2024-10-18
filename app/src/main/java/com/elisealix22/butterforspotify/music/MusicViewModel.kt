package com.elisealix22.butterforspotify.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.service.MusicService
import com.elisealix22.butterforspotify.ui.UiMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.toUiErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val musicService = MusicService()

    private val _uiState = MutableStateFlow<UiState<List<AlbumShelf>>>(UiState.Loading(null))
    val uiState: StateFlow<UiState<List<AlbumShelf>>> = _uiState.asStateFlow()

    fun fetchFeaturedPlaylists() {
        viewModelScope.launch {
            musicService.fetchTopAlbums()
                .onStart {
                    _uiState.value = UiState.Loading(data = _uiState.value.data)
                }
                .catch { error ->
                    _uiState.value = UiState.Error(
                        message = error.toUiErrorMessage(),
                        data = _uiState.value.data,
                        onTryAgain = { fetchFeaturedPlaylists() }
                    )
                }
                .collect { response ->
                    _uiState.value = UiState.Success(
                        listOf(
                            AlbumShelf(
                                message = UiMessage.MessageResId(R.string.the_rotation),
                                albums = response
                            )
                        )
                    )
                }
        }
    }
}
