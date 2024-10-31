package com.elisealix22.butterforspotify.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.elisealix22.butterforspotify.player.MockPlayer
import com.elisealix22.butterforspotify.player.Player
import com.elisealix22.butterforspotify.player.PlayerBar
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.LandscapeThemePreview
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun AdaptivePlayerBarScaffold(
    modifier: Modifier = Modifier,
    playerUiState: UiState<Player>,
    currentDestination: NavDestination?,
    onTabClick: (tab: BottomNavigationTab) -> Unit,
    navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
    containerColor: Color = NavigationSuiteScaffoldDefaults.containerColor,
    contentColor: Color = NavigationSuiteScaffoldDefaults.contentColor,
    content: @Composable () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
//    val containerWidth = with(LocalDensity.current) {
//        currentWindowSize().width.toDp().let {
//            it - (it - configuration.screenWidthDp.dp) +
//        }
//    }
    val containerWidth = LocalConfiguration.current.screenWidthDp.dp
    val containerHeight = with(LocalDensity.current) { currentWindowSize().height.toDp() }
    val isLandscape = containerWidth > containerHeight
    val layoutType = if (isLandscape) {
        NavigationSuiteType.NavigationRail
    } else {
        NavigationSuiteType.NavigationBar
    }
    val playerBarExpandOffset = remember { mutableFloatStateOf(0F) }
    val selectedItemColors = selectedItemColors()
    val unselectedItemColors = unselectedItemColors()
    val navigationBarSize = navigationBarSize(isLandscape)
    val verticalOffset = if (isLandscape) {
        0F
    } else {
        playerBarExpandOffset.floatValue * navigationBarSize.value
    }
    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        AdaptivePlayerBarLayout(
            navigationSuite = {
                NavigationSuite(
                    modifier = Modifier
                        .fixNavigationSize(isLandscape, navigationBarSize)
                        .offset { IntOffset(0, verticalOffset.dp.roundToPx()) },
                    layoutType = layoutType,
                    colors = navigationSuiteColors,
                    content = {
                        BottomNavigationTabs.forEach { tab ->
                            val isSelected = tab.isSelected(currentDestination)
                            item(
                                icon = { BottomNavigationIcon(tab = tab) },
                                label = { BottomNavigationText(tab = tab) },
                                selected = isSelected,
                                onClick = { onTabClick(tab) },
                                colors = if (isSelected) {
                                    selectedItemColors
                                } else {
                                    unselectedItemColors
                                },
                                enabled = playerBarExpandOffset.floatValue == 0F
                            )
                        }
                    }
                )
            },
            layoutType = layoutType,
            content = { content() },
            playerBar = {
                PlayerBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset { IntOffset(0, verticalOffset.dp.roundToPx()) },
                    playerUiState = playerUiState,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight,
                    bottomPadding = playerBarBottomPadding(isLandscape),
                    horizontalPadding = if (isLandscape) {
                        navigationBarSize.plus(Dimen.PaddingOneAndAHalf)
                    } else {
                        Dimen.PaddingOneAndAHalf
                    },
                    onExpandChange = { offset ->
                        playerBarExpandOffset.floatValue = offset
                    }
                )
            }
        )
    }
}

@Composable
private fun playerBarBottomPadding(isLandscape: Boolean): Dp {
    return if (isLandscape) {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }
}

@Composable
private fun navigationBarSize(isLandscape: Boolean): Dp {
    val navBarSize = 80.dp
    return if (isLandscape) {
        navBarSize
    } else {
        navBarSize.plus(WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
    }
}

@Composable
private fun Modifier.fixNavigationSize(isLandscape: Boolean, navigationBarSize: Dp): Modifier {
    return if (isLandscape) width(navigationBarSize) else height(navigationBarSize)
}

@Composable
private fun selectedItemColors(): NavigationSuiteItemColors =
    NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors().copy(
            disabledIconColor = NavigationBarItemDefaults.colors().selectedIconColor,
            disabledTextColor = NavigationBarItemDefaults.colors().selectedTextColor
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors().copy(
            disabledIconColor = NavigationRailItemDefaults.colors().selectedIconColor,
            disabledTextColor = NavigationBarItemDefaults.colors().selectedTextColor
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors()
    )

@Composable
private fun unselectedItemColors(): NavigationSuiteItemColors =
    NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors().copy(
            disabledIconColor = NavigationBarItemDefaults.colors().unselectedIconColor,
            disabledTextColor = NavigationBarItemDefaults.colors().unselectedTextColor
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors().copy(
            disabledIconColor = NavigationRailItemDefaults.colors().unselectedIconColor,
            disabledTextColor = NavigationBarItemDefaults.colors().unselectedTextColor
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors()
    )

@ThemePreview
@Composable
fun BottomNavigationPreview() {
    ButterForSpotifyTheme {
        NavigationSuite {
            BottomNavigationTabs.forEachIndexed { index, tab ->
                item(
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
fun AdaptivePlayerBarScaffoldPortraitPreview() {
    ButterForSpotifyTheme {
        AdaptivePlayerBarScaffold(
            currentDestination = null,
            onTabClick = {},
            playerUiState = UiState.Success(MockPlayer)
        ) {
            Text(modifier = Modifier.padding(Dimen.Padding), text = "Content")
        }
    }
}

@SuppressLint("RestrictedApi")
@LandscapeThemePreview
@Composable
fun AdaptivePlayerBarScaffoldLandscapePreview() {
    ButterForSpotifyTheme {
        AdaptivePlayerBarScaffold(
            currentDestination = null,
            onTabClick = {},
            playerUiState = UiState.Success(MockPlayer)
        ) {
            Text(modifier = Modifier.padding(Dimen.Padding), text = "Content")
        }
    }
}
