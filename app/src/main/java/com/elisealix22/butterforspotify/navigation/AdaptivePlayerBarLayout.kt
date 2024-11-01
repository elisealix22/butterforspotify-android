package com.elisealix22.butterforspotify.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastFirst

private const val NAVIGATION_TAG = "adaptiveNavigation"
private const val CONTENT_TAG = "adaptiveContent"
private const val PLAYER_BAR_TAG = "adaptivePlayerBar"

@Composable
private fun leftNavigationPadding(): Dp =
    WindowInsets.displayCutout.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr).plus(
        WindowInsets.systemBars.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr)
    )

@Composable
private fun rightNavigationPadding(): Dp =
    WindowInsets.displayCutout.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr).plus(
        WindowInsets.systemBars.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr)
    )

@Composable
private fun bottomNavigationPadding(): Dp =
    WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

@Composable
fun AdaptivePlayerBarLayout(
    navigationSuite: @Composable (bottomNavigationPadding: Dp) -> Unit,
    layoutType: NavigationSuiteType,
    content: @Composable () -> Unit = {},
    playerBar: @Composable BoxScope.(bottomNavigationPadding: Dp) -> Unit = {}
) {
    val bottomNavigationPadding = bottomNavigationPadding()
    Layout({
        Box(
            modifier = Modifier
                .layoutId(NAVIGATION_TAG)
                .absolutePadding(left = leftNavigationPadding())
                .consumeWindowInsets(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal.plus(WindowInsetsSides.Bottom))
                )
        ) {
            navigationSuite(bottomNavigationPadding)
        }
        Box(
            modifier = Modifier.layoutId(CONTENT_TAG)
                .absolutePadding(right = rightNavigationPadding())
                .consumeWindowInsets(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal.plus(WindowInsetsSides.Bottom))
                )
        ) {
            content()
        }
        Box(Modifier.fillMaxSize().layoutId(PLAYER_BAR_TAG)) { playerBar(bottomNavigationPadding) }
    }) { measurables, constraints ->
        val navigationPlaceable = measurables.fastFirst { it.layoutId == NAVIGATION_TAG }
            .measure(constraints.copy(minWidth = 0, minHeight = 0))
        val playerBarPlaceable = measurables.fastFirst { it.layoutId == PLAYER_BAR_TAG }
            .measure(constraints.copy(minWidth = 0, minHeight = 0))
        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth
        val isNavigationBar = layoutType == NavigationSuiteType.NavigationBar
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
                contentPlaceable.place(x = 0, y = 0)
                playerBarPlaceable.place(
                    x = 0,
                    y = layoutHeight - navigationPlaceable.height - playerBarPlaceable.height
                )
                navigationPlaceable.place(
                    x = 0,
                    y = layoutHeight - navigationPlaceable.height
                )
            } else {
                navigationPlaceable.place(x = 0, y = 0)
                contentPlaceable.place(x = navigationPlaceable.width, y = 0)
                playerBarPlaceable.place(x = 0, y = 0)
            }
        }
    }
}
