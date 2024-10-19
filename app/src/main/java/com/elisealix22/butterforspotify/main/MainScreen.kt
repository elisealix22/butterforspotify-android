package com.elisealix22.butterforspotify.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults.windowInsets
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.music.MusicScreen
import com.elisealix22.butterforspotify.navigation.BottomNavigationTab
import com.elisealix22.butterforspotify.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.navigation.ButterRoute
import com.elisealix22.butterforspotify.player.MockPlayer
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.player.PlayerBar
import com.elisealix22.butterforspotify.player.PlayerBarHeight
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    if (screenWidth > screenHeight) {
        LandscapeUi(
            playerUiState = playerUiState,
            navController = navController,
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
        )
    } else {
        PortraitUi(
            playerUiState = playerUiState,
            navController = navController,
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
        )
    }
//    val layoutType = if (screenWidth > screenHeight) NavigationSuiteType.NavigationRail else NavigationSuiteType.NavigationBar
//    NavigationSuiteScaffold(
//        modifier = modifier,
//        layoutType = layoutType,
//        navigationSuiteItems = {
//            BottomNavigationTabs.forEach { tab ->
//                item(
//                    icon = {
//                        Icon(
//                            painter = painterResource(tab.iconResId),
//                            contentDescription = stringResource(tab.name)
//                        )
//                    },
//                    label = {
//                        Text(stringResource(tab.name))
//                    },
//                    selected = currentDestination?.hierarchy?.any {
//                        it.hasRoute(tab.route::class)
//                    } == true,
//                    onClick = {
//                        navController.navigate(tab.route) {
//                            popUpTo(navController.graph.findStartDestination().id) {
//                                saveState = true
//                            }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                )
//            }
//        }
////        bottomBar = {
////            Column {
////                PlayerBar(playerUiState = playerUiState)
////                BottomNavigationBar(navController)
////            }
////        }
////    ) { innerPadding ->
//        ) {
//        Column(modifier = Modifier.fillMaxHeight()) {
//            NavHost(
//                modifier = Modifier.weight(1F),
//                navController = navController,
//                startDestination = BottomNavigationTabs.first().route,
////                modifier = Modifier.padding(innerPadding)
//            ) {
//                composable<ButterRoute.Music> {
//                    MusicScreen(playerUiState = playerUiState)
//                }
//                composable<ButterRoute.Audio> {
//                    Text(
//                        modifier = Modifier.padding(Dimen.Padding),
//                        text = "Episodes coming soon."
//                    )
//                }
//            }
//            val bars = WindowInsets.navigationBars.asPaddingValues()
//            Log.e("####", "bars: $bars")
//            val default = NavigationRailDefaults.windowInsets.asPaddingValues()
//            Log.e("###", "Default: $default")
//            val start = NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start).asPaddingValues()
//            Log.e("###", "Start: $start")
//            val windowInsets = WindowInsets.navigationBars.asPaddingValues().calculateStartPadding(LayoutDirection.Ltr)
//            PlayerBar(playerUiState = playerUiState, modifier = Modifier.padding(end = 80.dp))
//        }
//    }
}

@Composable
private fun LandscapeUi(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navController: NavHostController,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    val navigationRailWidth = 80.dp
    Row(modifier = modifier.fillMaxWidth()) {
        NavigationRail(modifier = Modifier.width(navigationRailWidth)) {
            BottomNavigationTabs.forEach { tab ->
                NavigationRailItem(
                    icon = {
                        Icon(
                            painter = painterResource(tab.iconResId),
                            contentDescription = stringResource(tab.name)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.name),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(tab.route::class)
                    } == true,
                    onClick = { onTabClick(tab) }
                )
            }
        }
        Box(modifier = Modifier.fillMaxHeight()) {
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
            PlayerBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = navigationRailWidth),
                playerUiState = playerUiState
            )
        }
    }
}

@Composable
private fun PortraitUi(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    navController: NavHostController,
    onTabClick: (BottomNavigationTab) -> Unit
) {
    Scaffold(
        bottomBar = {
            Column {
                PlayerBar(playerUiState = playerUiState)
                BottomNavigationBar(navController)
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
private fun BottomNavigationBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        BottomNavigationTabs.forEach { tab ->
            BottomNavigationBarItem(
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
private fun RowScope.BottomNavigationBarItem(
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
                BottomNavigationBarItem(
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
        MainScreen(playerUiState = UiState.Success(MockPlayer))
    }
}
