package com.elisealix22.butterforspotify.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.auth.AuthService
import com.elisealix22.butterforspotify.ui.UiMessage
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.toUiErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    private val authService = AuthService()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Initial())
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private var fetchTokenJob: Job? = null

    fun fetchAccessToken(code: String) {
        fetchTokenJob?.cancel()
        fetchTokenJob = viewModelScope.launch(Dispatchers.IO) {
            authService.fetchAuthToken(
                code = code
            ).onStart {
                _uiState.value = UiState.Loading(data = Unit)
            }.catch { error ->
                _uiState.value = UiState.Error(
                    data = Unit,
                    message = error.toUiErrorMessage(),
                    onTryAgain = null,
                    showInSnackbar = true
                )
            }.collect {
                _uiState.value = UiState.Success(Unit)
            }
        }
    }

    fun resetSpotifyAuth() {
        _uiState.value = UiState.Initial(Unit)
    }

    fun showSpotifyAuthLoading() {
        _uiState.value = UiState.Loading(Unit)
    }

    fun showSpotifyAuthError(error: String) {
        _uiState.value = UiState.Error(
            data = Unit,
            message = UiMessage.Message(error),
            onTryAgain = null,
            showInSnackbar = true
        )
    }
}
