package com.elisealix22.butterforspotify.data.model.album

import com.squareup.moshi.Json

data class Album(
    val id: String,
    @Json(name = "album_type")
    val albumType: AlbumType,
    val name: String
)
