package com.elisealix22.butterforspotify.data.model.user

import com.elisealix22.butterforspotify.data.model.SpotifyImage
import com.squareup.moshi.Json

data class User(
    val id: String,
    @Json(name = "display_name")
    val displayName: String?,
    val uri: String,
    val images: List<SpotifyImage>
)
