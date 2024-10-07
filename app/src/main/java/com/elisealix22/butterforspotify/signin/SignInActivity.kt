package com.elisealix22.butterforspotify.signin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.main.MainActivity
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.math.BigInteger
import java.security.SecureRandom
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {

    // https://developer.spotify.com/documentation/web-api/concepts/scopes
    private val scopes = arrayOf(
        "app-remote-control",
        "ugc-image-upload",
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing",
        "playlist-read-private",
        "playlist-read-collaborative",
        "playlist-modify-private",
        "playlist-modify-public",
        "user-follow-modify",
        "user-follow-read",
        "user-read-playback-position",
        "user-top-read",
        "user-read-recently-played",
        "user-library-modify",
        "user-library-read",
        "user-read-private"
    )

    private val state = BigInteger(130, SecureRandom()).toString(32)

    private val authCodeRequest
        get() = AuthorizationRequest.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.CODE,
            BuildConfig.SPOTIFY_REDIRECT_URI
        ).apply {
            setScopes(scopes)
            setState(state)
        }.build()

    private val spotifyLogin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
        handleResponse(response)
    }

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState is UiState.Success) {
                        openApp()
                    }
                }
            }
        }
        setContent {
            ButterForSpotifyTheme {
                SignInScreen(
                    viewModel = viewModel,
                    onSignInClick = { signIn() }
                )
            }
        }
    }

    private fun signIn() {
        viewModel.showSpotifyAuthLoading()
        spotifyLogin.launch(
            AuthorizationClient.createLoginActivityIntent(this, authCodeRequest)
        )
    }

    private fun handleResponse(response: AuthorizationResponse) {
        when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                if (response.state != state) {
                    viewModel.showSpotifyAuthError(getString(R.string.sign_in_error_invalid_state))
                } else {
                    viewModel.fetchAccessToken(response.code)
                }
            }
            AuthorizationResponse.Type.EMPTY -> {
                viewModel.resetSpotifyAuth()
            }
            AuthorizationResponse.Type.TOKEN,
            AuthorizationResponse.Type.ERROR,
            AuthorizationResponse.Type.UNKNOWN,
            null -> {
                viewModel.showSpotifyAuthError(getString(R.string.sign_in_error_response))
            }
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}
