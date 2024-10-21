package com.elisealix22.butterforspotify.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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

// TODO(elise): Private
val NavigationBarSize = 80.dp

@Composable
fun MainScreen(
    playerUiState: UiState<Player>
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
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
            navHostController = navController,
            currentDestination = currentDestination,
            onTabClick = onTabClick
        )
    } else {
        PortraitScaffold(
            playerUiState = playerUiState,
            navHostController = navController,
            currentDestination = currentDestination,
            onTabClick = onTabClick
        )
    }
}

@Composable
private fun LandscapeScaffold(
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navHostController: NavHostController,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier
                .fillMaxHeight()
                .width(NavigationBarSize)
                .align(Alignment.TopStart)
        ) {
            BottomNavigationTabs.forEach { tab ->
                NavigationRailItem(
                    icon = { BottomNavigationIcon(tab = tab) },
                    label = { BottomNavigationText(tab = tab) },
                    selected = tab.isSelected(currentDestination),
                    onClick = { onTabClick(tab) }
                )
            }
        }
        MainNavHost(
            modifier = Modifier.padding(start = NavigationBarSize),
            navHostController = navHostController,
            playerUiState = playerUiState
        )
        PlayerBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            playerUiState = playerUiState
        )
    }
}

@Composable
private fun PortraitScaffold(
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navHostController: NavHostController,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MainNavHost(
            modifier = Modifier.padding(bottom = NavigationBarSize),
            navHostController = navHostController,
            playerUiState = playerUiState
        )
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(NavigationBarSize)
                .align(Alignment.BottomStart)
        ) {
            BottomNavigationTabs.forEach { tab ->
                NavigationBarItem(
                    icon = { BottomNavigationIcon(tab = tab) },
                    label = { BottomNavigationText(tab = tab) },
                    selected = tab.isSelected(currentDestination),
                    onClick = { onTabClick(tab) }
                )
            }
        }
        PlayerBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            playerUiState = playerUiState
        )
    }
}

@Composable
private fun MainNavHost(
    modifier: Modifier,
    navHostController: NavHostController,
    playerUiState: UiState<Player>
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
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
