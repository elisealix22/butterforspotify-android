package com.elisealix22.butterforspotify.data.model.track

data class TopTracksResponse(
    val items: List<Track>,
    val total: Int,
    val next: String?
)
