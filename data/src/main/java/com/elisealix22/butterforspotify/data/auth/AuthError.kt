package com.elisealix22.butterforspotify.data.auth

import com.squareup.moshi.Json

internal data class AuthError(
    @Json(name = "error_description")
    val errorDescription: String
)
