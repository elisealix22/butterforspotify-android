package com.elisealix22.butterforspotify.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.util.fastFirst

private const val NAVIGATION_TAG = "adaptiveNavigation"
private const val CONTENT_TAG = "adaptiveContent"
private const val PLAYER_BAR_TAG = "adaptivePlayerBar"

@Composable
fun AdaptivePlayerBarLayout(
    navigationSuite: @Composable () -> Unit,
    layoutType: NavigationSuiteType,
    content: @Composable () -> Unit = {},
    playerBar: @Composable BoxScope.() -> Unit = {}
) {
    Layout({
        Box(Modifier.layoutId(NAVIGATION_TAG)) { navigationSuite() }
        Box(Modifier.layoutId(CONTENT_TAG)) { content() }
        Box(Modifier.fillMaxSize().layoutId(PLAYER_BAR_TAG)) { playerBar() }
    }) { measurables, constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val navigationPlaceable =
            measurables
                .fastFirst { it.layoutId == NAVIGATION_TAG }
                .measure(looseConstraints)
        val playerBarPlaceable = measurables.fastFirst { it.layoutId == PLAYER_BAR_TAG }
            .measure(looseConstraints)
        val isNavigationBar = layoutType == NavigationSuiteType.NavigationBar
        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth
        val contentPlaceable =
            measurables
                .fastFirst { it.layoutId == CONTENT_TAG }
                .measure(
                    if (isNavigationBar) {
                        constraints.copy(
                            minHeight = layoutHeight - navigationPlaceable.height,
                            maxHeight = layoutHeight - navigationPlaceable.height
                        )
                    } else {
                        constraints.copy(
                            minWidth = layoutWidth - navigationPlaceable.width,
                            maxWidth = layoutWidth - navigationPlaceable.width
                        )
                    }
                )

        layout(layoutWidth, layoutHeight) {
            if (isNavigationBar) {
                contentPlaceable.placeRelative(x = 0, y = 0)
                playerBarPlaceable.placeRelative(
                    x = 0,
                    y = layoutHeight - navigationPlaceable.height - playerBarPlaceable.height
                )
                navigationPlaceable.placeRelative(
                    x = 0,
                    y = layoutHeight - navigationPlaceable.height
                )
            } else {
                navigationPlaceable.placeRelative(x = 0, y = 0)
                contentPlaceable.placeRelative(x = navigationPlaceable.width, y = 0)
                playerBarPlaceable.placeRelative(x = 0, y = 0)
            }
        }
    }
}
