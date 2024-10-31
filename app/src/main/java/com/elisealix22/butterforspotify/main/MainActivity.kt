package com.elisealix22.butterforspotify.main

import android.app.ActionBar.LayoutParams
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elisealix22.butterforspotify.data.auth.AuthStore
import com.elisealix22.butterforspotify.player.PlayerViewModel
import com.elisealix22.butterforspotify.signin.SignInActivity
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthStore.isAuthenticated) {
            signOut()
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

        enableEdgeToEdge()

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

    private fun signOut() {
        startActivity(
            Intent(this, SignInActivity::class.java),
            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
        )
        finish()
    }
}
