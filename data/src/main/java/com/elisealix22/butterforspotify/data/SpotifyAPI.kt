package com.elisealix22.butterforspotify.data

import com.elisealix22.butterforspotify.data.playlist.FeaturedPlaylists
import retrofit2.http.GET

interface SpotifyAPI {

    @GET("browse/featured-playlists")
    fun featuredPlaylists() : FeaturedPlaylists
}
