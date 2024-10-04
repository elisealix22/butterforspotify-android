package com.elisealix22.butterforspotify.main

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elisealix22.butterforspotify.data.auth.AuthStore
import com.elisealix22.butterforspotify.signin.SignInActivity
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthStore.isAuthenticated) {
            signOut()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthStore.authenticatedFlow.stateIn(this).collect { isAuthenticated ->
                    Log.d(TAG, "Authenticated flow emitted $isAuthenticated")
                    if (!isAuthenticated) signOut()
                }
            }
        }

        setContent {
            ButterForSpotifyTheme {
                MainScreen()
            }
        }
    }

    private fun signOut() {
        startActivity(
            Intent(this, SignInActivity::class.java),
            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
        )
        finish()
    }
}
