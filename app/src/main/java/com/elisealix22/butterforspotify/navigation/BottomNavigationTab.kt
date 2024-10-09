package com.elisealix22.butterforspotify.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.elisealix22.butterforspotify.R

data class BottomNavigationTab(
    @StringRes val name: Int,
    @DrawableRes val iconResId: Int,
    val route: ButterRoute
)

val BottomNavigationTabs = listOf(
    BottomNavigationTab(R.string.music_tab, R.drawable.ic_disc, ButterRoute.Music),
    BottomNavigationTab(R.string.audio_tab, R.drawable.ic_mic, ButterRoute.Audio)
)
