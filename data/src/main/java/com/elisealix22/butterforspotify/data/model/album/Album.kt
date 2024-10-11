package com.elisealix22.butterforspotify.data.model.album

import com.elisealix22.butterforspotify.data.model.SpotifyImage
import com.elisealix22.butterforspotify.data.model.artist.Artist
import com.squareup.moshi.Json

data class Album(
    val id: String,
    @Json(name = "album_type")
    val albumType: AlbumType,
    val name: String,
    val images: List<SpotifyImage>,
    val artists: List<Artist>,
    val uri: String,
    @Json(name = "release_date")
    val releaseDate: String,
    @Json(name = "release_date_precision")
    val releaseDatePrecision: ReleaseDatePrecision,
    @Json(name = "total_tracks")
    val totalTracks: Int
)
