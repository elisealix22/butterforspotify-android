package com.elisealix22.butterforspotify.data.service

import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.elisealix22.butterforspotify.data.model.album.Album
import com.elisealix22.butterforspotify.data.model.track.TopTracksResponse
import com.elisealix22.butterforspotify.data.util.fetchFromNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip

class MusicService {

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks
     */
    @Throws(ServiceError::class)
    suspend fun fetchTopAlbums(): Flow<List<Album>> =
        fetchTopTracksForRange(TopTracksTimeRange.SHORT_TERM)
            .zip(fetchTopTracksForRange(TopTracksTimeRange.MEDIUM_TERM)) { short, medium ->
                short.items
                    .map { it.album }
                    .plus(medium.items.map { it.album })
                    .distinctBy { it.id }
            }

    private enum class TopTracksTimeRange(val value: String) {
        SHORT_TERM("short_term"),
        MEDIUM_TERM("medium_term"),
        LONG_TERM("long_term")
    }

    @Throws(ServiceError::class)
    private suspend fun fetchTopTracksForRange(
        timeRange: TopTracksTimeRange
    ): Flow<TopTracksResponse> =
        SpotifyClient.api
            .topTracks(timeRange = timeRange.value, limit = 30)
            .fetchFromNetwork()
            .flowOn(Dispatchers.IO)
}
