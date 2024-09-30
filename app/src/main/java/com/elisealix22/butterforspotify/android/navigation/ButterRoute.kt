package com.elisealix22.butterforspotify.android.navigation

import kotlinx.serialization.Serializable

sealed class ButterRoute {
    @Serializable
    data object Music : ButterRoute()
    @Serializable
    data object Profile : ButterRoute()
}
