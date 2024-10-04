package com.elisealix22.butterforspotify.data.playlist

import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.elisealix22.butterforspotify.data.util.fetchFromNetwork
import kotlinx.coroutines.flow.Flow

class PlaylistService {

    @Throws(ServiceError::class)
    suspend fun fetchFeaturedPlaylists(): Flow<FeaturedPlaylists> =
        SpotifyClient.api
            .featuredPlaylists()
            .fetchFromNetwork()
}
