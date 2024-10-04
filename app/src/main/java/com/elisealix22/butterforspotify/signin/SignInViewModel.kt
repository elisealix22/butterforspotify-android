package com.elisealix22.butterforspotify.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.auth.AuthService
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

    private val _uiState = MutableStateFlow(false)
    val uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    private var fetchTokenJob: Job? = null

    fun fetchAccessToken(code: String) {
        fetchTokenJob?.cancel()
        fetchTokenJob = viewModelScope.launch(Dispatchers.IO) {
            authService.fetchAuthToken(
                code = code
            ).onStart {
                // TODO(elise): Show loading
            }.catch { error ->
                // TODO(elise): Handle error & add test
            }.collect { success ->
                _uiState.value = success
            }
        }
    }
}
