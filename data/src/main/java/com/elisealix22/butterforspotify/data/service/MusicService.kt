package com.elisealix22.butterforspotify.data.service

import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.elisealix22.butterforspotify.data.model.track.TopTracksResponse
import com.elisealix22.butterforspotify.data.util.fetchFromNetwork
import kotlinx.coroutines.flow.Flow

class MusicService {

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks
     */
    @Throws(ServiceError::class)
    suspend fun fetchTopTracks(): Flow<TopTracksResponse> =
        SpotifyClient.api
            .topTracks(timeRange = "medium_term", limit = 20)
            .fetchFromNetwork()
}
