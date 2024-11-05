package com.elisealix22.butterforspotify.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.auth.AuthService
import com.elisealix22.butterforspotify.data.auth.AuthStore
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val authService = AuthService()

    fun verifyAppRemoteUserSignedIn(response: AuthorizationResponse) {
        viewModelScope.launch {
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    authService.verifyAppRemoteUserSignedIn(response.accessToken).firstOrNull()
                }
                AuthorizationResponse.Type.CODE,
                AuthorizationResponse.Type.ERROR,
                AuthorizationResponse.Type.EMPTY,
                AuthorizationResponse.Type.UNKNOWN,
                null -> {
                    Log.e(TAG, "Unable to retrieve token from Spotify.")
                    AuthStore.clearActiveTokens()
                }
            }
        }
    }
}
