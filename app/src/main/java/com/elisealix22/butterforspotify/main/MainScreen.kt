package com.elisealix22.butterforspotify.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.navigation.ButterRoute
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                BottomNavigationTabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { },
                        label = { Text(stringResource(tab.name)) },
                        selected = currentDestination?.hierarchy?.any {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationTabs.first().route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ButterRoute.Music> {
                Text(
                    text = "Hello Music tab!",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable<ButterRoute.Profile> {
                Text(
                    text = "Hello Profile tab!",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@ThemePreview
@Composable
fun SignInScreenPreview() {
    ButterForSpotifyTheme {
        MainScreen()
    }
}
