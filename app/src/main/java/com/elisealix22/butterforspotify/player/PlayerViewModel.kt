package com.elisealix22.butterforspotify.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.toUiErrorMessage
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
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

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val spotifyConnectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
        .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
        .showAuthView(false)
        .build()

    private val spotifyAppRemote = MutableStateFlow<SpotifyAppRemote?>(null)
    private var spotifyPlayerStateSubscription: Subscription<PlayerState>? = null

    private val _uiState = MutableStateFlow<UiState<Player>>(UiState.Loading(null))
    val uiState: StateFlow<UiState<Player>> = _uiState.asStateFlow()

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
    }

    fun connect() {
        viewModelScope.launch {
            connectSpotifyAppRemote()
                .onStart {
                    Log.d(TAG, "Connecting to Spotify app remote")
                    val cachedPlayerState = _uiState.value.data?.playerState.let {
                        if (it == null) null else Player(playerState = it, spotifyApis = null)
                    }
                    _uiState.value = UiState.Loading(data = cachedPlayerState)
                }
                .onCompletion {
                    Log.d(TAG, "App remote channel completed")
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
                    spotifyAppRemote.value = remote
                    remote.observePlayerState()
                }
        }
    }

    private fun SpotifyAppRemote.observePlayerState() {
        spotifyPlayerStateSubscription?.cancel()
        spotifyPlayerStateSubscription = this.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState ->
                Log.d(TAG, "PlayerState callback received")
                _uiState.value = UiState.Success(
                    Player(
                        playerState = playerState,
                        spotifyApis = this.toSpotifyApis()
                    )
                )
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
            spotifyConnectionParams,
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
        spotifyPlayerStateSubscription?.cancel()
        spotifyAppRemote.value?.let {
            Log.d(TAG, "Disconnected from Spotify app remote")
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote.value = null
        }
    }
}
