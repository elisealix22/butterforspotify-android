package com.elisealix22.butterforspotify.data.model.track

import com.elisealix22.butterforspotify.data.model.album.Album

data class Track(
    val id: String,
    val name: String,
    val album: Album
)
