package com.elisealix22.butterforspotify.data.auth

import com.squareup.moshi.Json

internal data class AuthResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "refresh_token")
    val refreshToken: String?
)
