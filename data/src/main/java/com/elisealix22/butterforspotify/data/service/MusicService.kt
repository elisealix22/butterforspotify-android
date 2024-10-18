package com.elisealix22.butterforspotify.data.service

import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.elisealix22.butterforspotify.data.model.album.Album
import com.elisealix22.butterforspotify.data.model.album.AlbumStack
import com.elisealix22.butterforspotify.data.model.album.StackCategory
import com.elisealix22.butterforspotify.data.model.track.TopTracksResponse
import com.elisealix22.butterforspotify.data.model.track.TopTracksTimeRange
import com.elisealix22.butterforspotify.data.util.fetchFromNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.zip

class MusicService {

    @Throws(ServiceError::class)
    suspend fun fetchMusic(): Flow<List<AlbumStack>> =
        fetchTopAlbums()
            .zip(fetchRecentlyPlayed()) { top, recent ->
                listOf(
                    AlbumStack(StackCategory.TOP, top),
                    AlbumStack(StackCategory.RECENT, recent)
                )
            }
            .zip(fetchNewReleases()) { list, new ->
                list + AlbumStack(StackCategory.NEW, new)
            }
            .zip(fetchSavedAlbums()) { list, saved ->
                list + AlbumStack(StackCategory.SAVED, saved)
            }

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-recently-played
     */
    @Throws(ServiceError::class)
    private suspend fun fetchRecentlyPlayed(): Flow<List<Album>> =
        SpotifyClient.api
            .recentlyPlayed(limit = 20)
            .fetchFromNetwork()
            .flowOn(Dispatchers.IO)
            .mapNotNull {
                it.items.map { history ->
                    history.track.album
                }.distinctBy { album ->
                    album.id
                }
            }

    @Throws(ServiceError::class)
    private suspend fun fetchTopAlbums(): Flow<List<Album>> =
        fetchTopTracksForRange(TopTracksTimeRange.SHORT_TERM)
            .zip(fetchTopTracksForRange(TopTracksTimeRange.MEDIUM_TERM)) { short, medium ->
                short.items
                    .map { it.album }
                    .plus(medium.items.map { it.album })
                    .distinctBy { it.id }
            }

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks
     */
    @Throws(ServiceError::class)
    private suspend fun fetchTopTracksForRange(
        timeRange: TopTracksTimeRange
    ): Flow<TopTracksResponse> =
        SpotifyClient.api
            .topTracks(timeRange = timeRange.value, limit = 20)
            .fetchFromNetwork()
            .flowOn(Dispatchers.IO)

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-new-releases
     */
    @Throws(ServiceError::class)
    private suspend fun fetchNewReleases(): Flow<List<Album>> =
        SpotifyClient.api
            .newReleases(limit = 20)
            .fetchFromNetwork()
            .flowOn(Dispatchers.IO)
            .map {
                it.albums.items
            }

    /**
     * https://developer.spotify.com/documentation/web-api/reference/get-users-saved-albums
     */
    @Throws(ServiceError::class)
    private suspend fun fetchSavedAlbums(): Flow<List<Album>> =
        SpotifyClient.api
            .savedAlbums(limit = 20)
            .fetchFromNetwork()
            .flowOn(Dispatchers.IO)
            .map {
                it.items.map { saved -> saved.album }
            }
}
