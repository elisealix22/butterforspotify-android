package com.elisealix22.butterforspotify.music

import com.elisealix22.butterforspotify.data.model.album.Album
import com.elisealix22.butterforspotify.ui.UiMessage

data class AlbumShelf(
    val message: UiMessage,
    val albums: List<Album>
)
