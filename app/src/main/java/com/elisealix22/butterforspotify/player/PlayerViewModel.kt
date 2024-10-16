package com.elisealix22.butterforspotify.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.isLoadingOrInitial
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlayerViewModel"
        private const val APP_REMOTE_TAG = "SpotifyAppRemote"
        private const val PLAYER_STATE_TAG = "PlayerState"
    }

    private val spotifyConnectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
        .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
        .showAuthView(false)
        .build()

    private val spotifyAppRemote =
        MutableStateFlow<UiState<SpotifyAppRemote>>(UiState.Loading(null))
    private val playerState = MutableStateFlow<UiState<PlayerState>>(UiState.Loading(null))
    private val playerStateSubscription = MutableStateFlow<Subscription<PlayerState>?>(null)

    val uiState: StateFlow<UiState<Player>> =
        combine(spotifyAppRemote, playerState) { appRemote, state ->
            when {
                appRemote is UiState.Error -> {
                    UiState.Error<Player>(
                        data = null,
                        message = appRemote.message,
                        onTryAgain = appRemote.onTryAgain
                    )
                }
                state is UiState.Error -> {
                    UiState.Error<Player>(
                        data = null,
                        message = state.message,
                        onTryAgain = state.onTryAgain
                    )
                }
                appRemote.isLoadingOrInitial() || state.isLoadingOrInitial() -> {
                    val cachedPlayerState = state.data.let {
                        if (it == null) null else Player(playerState = it, spotifyApis = null)
                    }
                    UiState.Loading(data = cachedPlayerState)
                }
                appRemote is UiState.Success && state is UiState.Success -> {
                    UiState.Success(
                        data = Player(
                            spotifyApis = appRemote.data.toSpotifyApis(),
                            playerState = state.data
                        )
                    )
                }
                else -> {
                    UiState.Error<Player>(
                        data = null,
                        message = IllegalStateException("Unexpected combined state")
                            .toUiErrorMessage(),
                        onTryAgain = { connect() }
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState.Loading(null)
        )

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
    }

    fun connect() {
        viewModelScope.launch {
            connectSpotifyAppRemote()
                .onStart {
                    Log.d(TAG, "Connecting to $APP_REMOTE_TAG")
                    spotifyAppRemote.value = UiState.Loading(null)
                }
                .onCompletion {
                    Log.d(TAG, "$APP_REMOTE_TAG channel completed")
                }
                .catch { error ->
                    Log.e(TAG, "Failed to connect to $APP_REMOTE_TAG", error)
                    spotifyAppRemote.value = UiState.Error(
                        data = null,
                        message = error.toUiErrorMessage(),
                        onTryAgain = { connect() }
                    )
                }
                .collect { remote ->
                    Log.d(TAG, "Connected to $APP_REMOTE_TAG")
                    spotifyAppRemote.value = UiState.Success(remote)
                    subscribeToSpotifyPlayerState()
                }
        }
    }

    fun disconnect() {
        playerStateSubscription.value?.let {
            Log.d(TAG, "Canceled $PLAYER_STATE_TAG subscription")
            it.cancel()
            playerStateSubscription.value = null
        }
        spotifyAppRemote.value.data?.let {
            Log.d(TAG, "Disconnected from $APP_REMOTE_TAG")
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote.value = UiState.Initial()
        }
    }

    @Throws(Exception::class)
    private suspend fun connectSpotifyAppRemote(): Flow<SpotifyAppRemote> = channelFlow {
        disconnect()
        SpotifyAppRemote.connect(
            getApplication(),
            spotifyConnectionParams,
            object : ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote) {
                    trySendBlocking(remote)
                    close()
                }
                override fun onFailure(throwable: Throwable) {
                    close(throwable)
                }
            }
        )
        awaitClose()
    }

    private fun subscribeToSpotifyPlayerState() {
        val remote = spotifyAppRemote.value.data
        if (remote == null) {
            playerState.value = UiState.Error(
                data = null,
                message = IllegalStateException(
                    "Can't subscribe to $PLAYER_STATE_TAG without $APP_REMOTE_TAG"
                ).toUiErrorMessage(),
                onTryAgain = { connect() }
            )
            return
        }
        playerState.value = UiState.Loading(null)
        playerStateSubscription.value?.cancel()
        playerStateSubscription.value = remote.playerApi
            .subscribeToPlayerState()
            .setEventCallback { state ->
                Log.d(TAG, "$PLAYER_STATE_TAG callback received")
                playerState.value = UiState.Success(state)
            }
            .setErrorCallback { error ->
                Log.e(TAG, "$PLAYER_STATE_TAG callback error", error)
                playerState.value = UiState.Error(
                    data = null,
                    message = error.toUiErrorMessage(),
                    onTryAgain = { subscribeToSpotifyPlayerState() }
                )
            } as Subscription<PlayerState>
    }
}
