package com.elisealix22.butterforspotify.navigation

import androidx.annotation.StringRes
import com.elisealix22.butterforspotify.R

data class BottomNavigationTab(@StringRes val name: Int, val route: ButterRoute)

val BottomNavigationTabs = listOf(
    BottomNavigationTab(R.string.music_tab, ButterRoute.Music),
    BottomNavigationTab(R.string.profile_tab, ButterRoute.Profile)
)
