package com.elisealix22.butterforspotify.signin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme

class SignInActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ButterForSpotifyTheme {
                SignInScreen(
                    onSignInClick = {

                    }
                )
            }
        }
    }
}
