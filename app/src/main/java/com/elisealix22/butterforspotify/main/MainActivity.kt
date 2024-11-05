package com.elisealix22.butterforspotify.main

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.auth.AuthStore
import com.elisealix22.butterforspotify.player.PlayerViewModel
import com.elisealix22.butterforspotify.signin.SignInActivity
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val playerViewModel: PlayerViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private val syncAppRemoteUserRequest
        get() = AuthorizationRequest.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            BuildConfig.SPOTIFY_REDIRECT_URI
        ).apply {
            setScopes(SignInActivity.scopes)
            setShowDialog(false)
        }.build()

    private val syncAppRemoteUserWithWebApi = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
        mainViewModel.verifyAppRemoteUserSignedIn(response)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthStore.isAuthenticated) {
            signOut(animate = false)
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthStore.authenticatedFlow.stateIn(this).collect { isAuthenticated ->
                    Log.d(TAG, "Authenticated: $isAuthenticated")
                    if (!isAuthenticated) signOut()
                }
            }
        }

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(scrim = Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
            }
        )

        setContent {
            ButterForSpotifyTheme {
                val playerUiState by playerViewModel.uiState.collectAsState()
                MainScreen(
                    playerUiState = playerUiState
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playerViewModel.connect()
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.disconnect()
    }

    // TODO(elise): This works! But it shows an ugly loading spinner. When should we call it?
    private fun syncAppRemoteAndWebApiUsers() {
        syncAppRemoteUserWithWebApi.launch(
            AuthorizationClient.createLoginActivityIntent(this, syncAppRemoteUserRequest)
        )
    }

    private fun signOut(animate: Boolean = true) {
        startActivity(
            Intent(this, SignInActivity::class.java),
            if (animate) {
                null
            } else {
                ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
            }
        )
        finish()
    }
}
