package com.elisealix22.butterforspotify.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.elisealix22.butterforspotify.R

data class BottomNavigationTab(
    @StringRes val name: Int,
    @DrawableRes val iconResId: Int,
    val route: ButterRoute
)

val BottomNavigationTabs = listOf(
    BottomNavigationTab(R.string.music_tab, R.drawable.ic_disc_24, ButterRoute.Music),
    BottomNavigationTab(R.string.audio_tab, R.drawable.ic_mic_24, ButterRoute.Audio)
)

fun BottomNavigationTab.isSelected(currentDestination: NavDestination?) =
    currentDestination?.hierarchy?.any {
        it.hasRoute(route::class)
    } == true
