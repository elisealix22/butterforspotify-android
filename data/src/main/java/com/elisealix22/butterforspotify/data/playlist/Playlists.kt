package com.elisealix22.butterforspotify.data.playlist

data class Playlists(
    val href: String,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val total: Int,
    val items: List<Playlist>
)
