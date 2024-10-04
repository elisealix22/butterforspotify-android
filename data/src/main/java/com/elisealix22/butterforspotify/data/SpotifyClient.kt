package com.elisealix22.butterforspotify.data

import com.elisealix22.butterforspotify.data.auth.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object SpotifyClient {

    private const val API_URL = "https://api.spotify.com/v1/"
    internal const val TOKEN_URL = "https://accounts.spotify.com/api/token/"

    private val authenticatedHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) }
                )
            }
        }
        .build()

    internal val unAuthenticatedHttpClient = OkHttpClient.Builder().build()

    internal val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_URL)
        .client(authenticatedHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    internal val api: SpotifyAPI = retrofit.create(SpotifyAPI::class.java)
}
