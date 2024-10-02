package com.elisealix22.butterforspotify.signin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.elisealix22.butterforspotify.main.MainActivity
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.auth.AuthStore
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.SecureRandom

class SignInActivity: ComponentActivity() {

    companion object {
        private const val SPOTIFY_AUTH_REDIRECT_URI = "butterforspotify://auth"
    }

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
        "user-read-private" // TODO(elise): do i need this one?
    )

    private val state = BigInteger(130, SecureRandom()).toString(32)

    private val authRequest
        get() = AuthorizationRequest.Builder(
            getString(R.string.spotify_client_id),
            AuthorizationResponse.Type.TOKEN,
            SPOTIFY_AUTH_REDIRECT_URI
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            AuthorizationClient.createLoginActivityIntent(this, authRequest)
        )
    }

    private fun handleResponse(response: AuthorizationResponse) {
        when (val result = parseResult(response)) {
            SignInResult.BadState -> Unit // TODO(elise): Show invalid state error
            SignInResult.Canceled -> Unit
            is SignInResult.Error -> Unit // TODO(elise): Show error message
            is SignInResult.Success -> lifecycleScope.launch {
                AuthStore.setActiveUserToken(result.accessToken)
                openApp()
            }
        }
    }

    private fun parseResult(response: AuthorizationResponse): SignInResult {
        if (response.state != state) {
            return SignInResult.BadState
        }
        return when (response.type) {
            AuthorizationResponse.Type.TOKEN -> SignInResult.Success(response.accessToken)
            AuthorizationResponse.Type.EMPTY -> SignInResult.Canceled
            AuthorizationResponse.Type.CODE,
            AuthorizationResponse.Type.ERROR,
            AuthorizationResponse.Type.UNKNOWN,
            null -> SignInResult.Error(response.error)
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}
