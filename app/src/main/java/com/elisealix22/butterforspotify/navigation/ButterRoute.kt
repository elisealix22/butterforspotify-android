package com.elisealix22.butterforspotify.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ButterRoute {

    @Serializable
    data object Music : ButterRoute()

    @Serializable
    data object Audio : ButterRoute()
}
