package com.elisealix22.butterforspotify.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.music.MusicScreen
import com.elisealix22.butterforspotify.navigation.BottomNavigationTab
import com.elisealix22.butterforspotify.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.navigation.ButterRoute
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.player.PlayerBar
import com.elisealix22.butterforspotify.ui.Player1
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MainScreen(
    playerUiState: UiState<Player>
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            Column {
                PlayerBar(playerUiState = playerUiState)
                NavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Column {
            NavHost(
                navController = navController,
                startDestination = BottomNavigationTabs.first().route,
                modifier = Modifier.padding(innerPadding)
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
}

@Composable
private fun NavigationBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        BottomNavigationTabs.forEach { tab ->
            NavigationBarItem(
                tab = tab,
                isSelected = currentDestination?.hierarchy?.any {
                    it.hasRoute(tab.route::class)
                } == true,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.NavigationBarItem(
    tab: BottomNavigationTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = {
            Icon(
                painter = painterResource(tab.iconResId),
                contentDescription = stringResource(tab.name)
            )
        },
        label = {
            Text(stringResource(tab.name))
        },
        selected = isSelected,
        onClick = onClick
    )
}

@ThemePreview
@Composable
fun TabPreviews() {
    ButterForSpotifyTheme {
        Row {
            BottomNavigationTabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    tab = tab,
                    isSelected = index == 0,
                    onClick = { }
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun MainScreenPreview() {
    ButterForSpotifyTheme {
        MainScreen(UiState.Success(Player1))
    }
}
