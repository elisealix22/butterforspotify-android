package com.elisealix22.butterforspotify.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.model.album.StackCategory
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

    fun fetchMusic() {
        viewModelScope.launch {
            musicService.fetchMusic()
                .onStart {
                    _uiState.value = UiState.Loading(data = _uiState.value.data)
                }
                .catch { error ->
                    _uiState.value = UiState.Error(
                        message = error.toUiErrorMessage(),
                        data = _uiState.value.data,
                        onTryAgain = { fetchMusic() }
                    )
                }
                .collect { response ->
                    val shelves = response.map { stack ->
                        when (stack.category) {
                            StackCategory.TOP -> {
                                AlbumShelf(
                                    message = UiMessage.MessageResId(R.string.your_rotation),
                                    albums = stack.albums
                                )
                            }
                            StackCategory.RECENT -> {
                                AlbumShelf(
                                    message = UiMessage.MessageResId(R.string.recently),
                                    albums = stack.albums
                                )
                            }
                            StackCategory.SAVED -> {
                                AlbumShelf(
                                    message = UiMessage.MessageResId(R.string.saved),
                                    albums = stack.albums
                                )
                            }
                        }
                    }
                    _uiState.value = UiState.Success(shelves)
                }
        }
    }
}
