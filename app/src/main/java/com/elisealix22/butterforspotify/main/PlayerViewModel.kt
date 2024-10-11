package com.elisealix22.butterforspotify.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.main.PlayerViewModel.SpotifyApis
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.toUiErrorMessage
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
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


fun SpotifyAppRemote.toSpotifyApis() = SpotifyApis(this.playerApi, this.imagesApi)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    data class SpotifyApis(
        val playerApi: PlayerApi,
        val imagesApi: ImagesApi
    )

    private val _spotifyAppRemote = MutableStateFlow<SpotifyAppRemote?>(null)
    val spotifyAppRemote: StateFlow<SpotifyAppRemote?> = _spotifyAppRemote.asStateFlow()

    private var playerStateSubscription: Subscription<PlayerState>? = null

    private val _uiState = MutableStateFlow<UiState<PlayerState>>(UiState.Loading(null))
    val uiState: StateFlow<UiState<PlayerState>> = _uiState.asStateFlow()

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
    }

    fun connect() {
        viewModelScope.launch {
            connectSpotifyAppRemote()
                .onStart {
                    Log.d(TAG, "Connecting to Spotify app remote")
                    _uiState.value = UiState.Loading(data = _uiState.value.data)
                }
                .onCompletion {
                    Log.d(TAG, "Channel completed")
                }
                .catch { error ->
                    Log.e(TAG, "Failed to connect to Spotify remote", error)
                    _uiState.value = UiState.Error(
                        data = null,
                        message = error.toUiErrorMessage()
                    )
                }
                .collect { remote ->
                    Log.d(TAG, "Connected to Spotify app remote")
                    _spotifyAppRemote.value = remote
                    remote.subscribe()
                }
        }
    }

    private fun SpotifyAppRemote.subscribe() {
        Log.d(TAG, "Subscribing to PlayerState")
        playerStateSubscription?.cancel()
        playerStateSubscription = this.playerApi
            .subscribeToPlayerState()
            .setEventCallback { callback ->
                Log.d(TAG, "PlayerState callback received")
                _uiState.value = UiState.Success(callback)
            }
            .setErrorCallback { error ->
                Log.e(TAG, "PlayerState callback error", error)
                _uiState.value = UiState.Error(
                    data = null,
                    message = error.toUiErrorMessage()
                )
            } as Subscription<PlayerState>
    }

    @Throws(Exception::class)
    private suspend fun connectSpotifyAppRemote(): Flow<SpotifyAppRemote> = channelFlow {
        disconnect()
        SpotifyAppRemote.connect(
            getApplication(),
            ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
                .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
                .showAuthView(false)
                .build(),
            object : ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote?) {
                    if (remote == null) {
                        close(IllegalStateException("Invalid Spotify app remote received"))
                        return
                    }
                    trySendBlocking(remote)
                    close()
                }

                override fun onFailure(throwable: Throwable?) {
                    close(throwable)
                }
            }
        )
        awaitClose()
    }

    fun disconnect() {
        playerStateSubscription?.cancel()
        spotifyAppRemote.value?.let {
            Log.d(TAG, "Disconnected from Spotify app remote")
            SpotifyAppRemote.disconnect(it)
            _spotifyAppRemote.value = null
        }
    }
}
