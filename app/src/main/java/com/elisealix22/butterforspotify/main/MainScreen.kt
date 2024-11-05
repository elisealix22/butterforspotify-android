package com.elisealix22.butterforspotify.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.music.MusicScreen
import com.elisealix22.butterforspotify.navigation.AdaptivePlayerBarScaffold
import com.elisealix22.butterforspotify.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.navigation.ButterRoute
import com.elisealix22.butterforspotify.player.MockPlayer
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.LandscapeThemePreview
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MainScreen(
    playerUiState: UiState<Player>,
    refreshWebApiUser: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AdaptivePlayerBarScaffold(
        playerUiState = playerUiState,
        currentDestination = currentDestination,
        onTabClick = { tab ->
            navController.navigate(tab.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = BottomNavigationTabs.first().route
        ) {
            composable<ButterRoute.Music> {
                MusicScreen(playerUiState = playerUiState)
            }
            composable<ButterRoute.Audio> {
                Text(
                    modifier = Modifier.padding(Dimen.Padding),
                    text = "Episodes coming soon."
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun MainScreenPortraitPreview() {
    ButterForSpotifyTheme {
        MainScreen(playerUiState = UiState.Success(MockPlayer))
    }
}

@LandscapeThemePreview
@Composable
fun MainScreenLandscapePreview() {
    ButterForSpotifyTheme {
        MainScreen(playerUiState = UiState.Success(MockPlayer))
    }
}
