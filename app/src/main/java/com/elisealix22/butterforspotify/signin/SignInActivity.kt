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
import com.elisealix22.butterforspotify.main.MainActivity
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.SecureRandom

class SignInActivity: ComponentActivity() {

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
                viewModel.uiState.collect { success ->
                    if (success) {
                        openApp()
                    }
                }
            }
        }
        setContent {
            ButterForSpotifyTheme {
                SignInScreen(
                    onSignInClick = { signIn() }
                )
            }
        }
    }

    private fun signIn() {
        spotifyLogin.launch(
            AuthorizationClient.createLoginActivityIntent(this, authCodeRequest)
        )
    }

    private fun handleResponse(response: AuthorizationResponse) {
        when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                viewModel.fetchAccessToken(response.code)
            }
            AuthorizationResponse.Type.EMPTY -> {
                // No-op. Sign in was canceled.
            }
            AuthorizationResponse.Type.TOKEN,
            AuthorizationResponse.Type.ERROR,
            AuthorizationResponse.Type.UNKNOWN,
            null -> {
                // TODO(elise): Show an error.
            }
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}
