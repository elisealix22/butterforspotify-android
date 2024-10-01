package com.elisealix22.butterforspotify.data

import com.elisealix22.butterforspotify.data.auth.AuthStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object SpotifyClient {

    private const val BASE_URL = "https://api.spotify.com"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${AuthStore.activeUserToken}")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    val api: SpotifyAPI = retrofit.create(SpotifyAPI::class.java)
}
