package com.elisealix22.butterforspotify.navigation

import kotlinx.serialization.Serializable

sealed class ButterRoute {
    @Serializable
    data object Music : ButterRoute()
    @Serializable
    data object Profile : ButterRoute()
}
