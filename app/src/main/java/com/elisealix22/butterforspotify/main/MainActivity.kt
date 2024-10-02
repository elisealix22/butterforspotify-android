package com.elisealix22.butterforspotify.main

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elisealix22.butterforspotify.signin.SignInActivity
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.data.auth.AuthStore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthStore.activeUserToken.isNullOrBlank()) {
            startActivity(
                Intent(this, SignInActivity::class.java),
                ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
            )
            finish()
            return
        }

        setContent {
            ButterForSpotifyTheme {
                MainScreen()
            }
        }
    }
}
