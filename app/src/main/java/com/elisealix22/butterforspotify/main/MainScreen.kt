package com.elisealix22.butterforspotify.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.music.MusicScreen
import com.elisealix22.butterforspotify.navigation.BottomNavigationIcon
import com.elisealix22.butterforspotify.navigation.BottomNavigationTab
import com.elisealix22.butterforspotify.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.navigation.BottomNavigationText
import com.elisealix22.butterforspotify.navigation.ButterRoute
import com.elisealix22.butterforspotify.navigation.isSelected
import com.elisealix22.butterforspotify.player.MockPlayer
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.player.PlayerBar
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.LandscapeThemePreview
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

private val LandscapeNavigationRailWidth = 80.dp

@Composable
fun MainScreen(
    playerUiState: UiState<Player>
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val navHost: @Composable () -> Unit = {
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
    val onTabClick: (BottomNavigationTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    if (screenWidth > screenHeight) {
        LandscapeScaffold(
            playerUiState = playerUiState,
            navHost = navHost,
            currentDestination = currentDestination,
            onTabClick = onTabClick
        )
    } else {
        PortraitScaffold(
            playerUiState = playerUiState,
            navHost = navHost,
            currentDestination = currentDestination,
            onTabClick = onTabClick
        )
    }
}

@Composable
private fun LandscapeScaffold(
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navHost: @Composable () -> Unit,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    val playerBarStartPadding = Dimen.Padding
    val playerBarEndPadding = LandscapeNavigationRailWidth + playerBarStartPadding
    Row(modifier = Modifier.fillMaxWidth()) {
        NavigationRail(modifier = Modifier.width(LandscapeNavigationRailWidth)) {
            BottomNavigationTabs.forEach { tab ->
                NavigationRailItem(
                    icon = { BottomNavigationIcon(tab = tab) },
                    label = { BottomNavigationText(tab = tab) },
                    selected = tab.isSelected(currentDestination),
                    onClick = { onTabClick(tab) }
                )
            }
        }
        Box(modifier = Modifier.fillMaxHeight()) {
            navHost()
            PlayerBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
//                    .padding(
//                        start = playerBarStartPadding,
//                        end = playerBarEndPadding
//                    ),
                playerUiState = playerUiState
            )
        }
    }
}

@Composable
private fun PortraitScaffold(
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navHost: @Composable () -> Unit,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavigationTabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { BottomNavigationIcon(tab = tab) },
                        label = { BottomNavigationText(tab = tab) },
                        selected = tab.isSelected(currentDestination),
                        onClick = { onTabClick(tab) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxHeight()
        ) {
            navHost()
            PlayerBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
//                    .padding(horizontal = Dimen.PaddingOneAndAHalf),
                playerUiState = playerUiState
            )
        }
    }
}

@ThemePreview
@Composable
fun BottomNavigationPreview() {
    ButterForSpotifyTheme {
        NavigationBar {
            BottomNavigationTabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    icon = { BottomNavigationIcon(tab = tab) },
                    label = { BottomNavigationText(tab = tab) },
                    selected = index == 0,
                    onClick = { }
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
