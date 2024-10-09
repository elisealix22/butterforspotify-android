package com.elisealix22.butterforspotify.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.ui.UiState
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AppRemoteViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AppRemoteViewModel"
    }

    @Volatile
    private var appRemote: SpotifyAppRemote? = null

    private val _appRemoteState = MutableStateFlow<UiState<Unit>>(UiState.Initial())
    val appRemoteState: StateFlow<UiState<Unit>> = _appRemoteState.asStateFlow()

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
        viewModelScope.launch {
            connectToSpotifyAppRemote()
                .onCompletion {
                    Log.d(TAG, "Channel closed")
                }
                .onStart {
                    Log.d(TAG, "Connecting to Spotify app remote")
                    _appRemoteState.value = UiState.Loading(Unit)
                }
                .catch { error ->
                    Log.e(TAG, "Failed to connect to Spotify remote", error)
                    _appRemoteState.value = UiState.Error(null)
                }
                .collect {
                    Log.d(TAG, "Connected to Spotify app remote")
                    _appRemoteState.value = UiState.Success(Unit)
                }
        }
    }

    @Throws(Exception::class)
    private suspend fun connectToSpotifyAppRemote(): Flow<Boolean> = channelFlow {
        disconnectSpotifyAppRemote()
        SpotifyAppRemote.connect(
            getApplication(),
            ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
                .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
                .showAuthView(false)
                .build(),
            object : ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote?) {
                    if (remote == null) {
                        close(IllegalStateException("Invalid SpotifyAppRemote"))
                        return
                    }
                    appRemote = remote
                    trySendBlocking(true)
                    close()
                }
                override fun onFailure(throwable: Throwable?) {
                    close(throwable)
                }
            }
        )
        awaitClose()
    }

    private fun disconnectSpotifyAppRemote() {
        if (appRemote != null) {
            Log.d(TAG, "Disconnected from Spotify app remote")
            SpotifyAppRemote.disconnect(appRemote)
            appRemote = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectSpotifyAppRemote()
    }
}
