package com.elisealix22.butterforspotify.data.playlist

import com.elisealix22.butterforspotify.data.SpotifyClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaylistService {

    suspend fun fetchFeaturedPlaylists(): Flow<FeaturedPlaylists> = flow {
        emit(SpotifyClient.api.featuredPlaylists())
    }
}
