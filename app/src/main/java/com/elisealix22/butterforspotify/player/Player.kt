package com.elisealix22.butterforspotify.player

import com.spotify.android.appremote.api.ImagesApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

data class Player(
    val playerState: PlayerState,
    val spotifyApis: SpotifyApis?
)

data class SpotifyApis(
    val playerApi: PlayerApi,
    val imagesApi: ImagesApi
)

fun SpotifyAppRemote.toSpotifyApis(): SpotifyApis = SpotifyApis(this.playerApi, this.imagesApi)
