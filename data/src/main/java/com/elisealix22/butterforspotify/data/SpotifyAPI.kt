package com.elisealix22.butterforspotify.data

import com.elisealix22.butterforspotify.data.model.track.TopTracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpotifyAPI {

    @GET("me/top/tracks")
    fun topTracks(
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int
    ): Call<TopTracksResponse>
}
