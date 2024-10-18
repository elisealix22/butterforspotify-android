package com.elisealix22.butterforspotify.data

import com.elisealix22.butterforspotify.data.auth.AuthInterceptor
import com.elisealix22.butterforspotify.data.model.album.AlbumType
import com.elisealix22.butterforspotify.data.model.album.ReleaseDatePrecision
import com.elisealix22.butterforspotify.data.model.playlist.HistoryContextType
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object SpotifyClient {

    private const val API_URL = "https://api.spotify.com/v1/"
    internal const val TOKEN_URL = "https://accounts.spotify.com/api/token/"

    private val authenticatedHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addDebugLoggingInterceptor()
        .build()

    internal val unAuthenticatedHttpClient = OkHttpClient.Builder()
        .addDebugLoggingInterceptor()
        .build()

    private fun OkHttpClient.Builder.addDebugLoggingInterceptor(): OkHttpClient.Builder =
        apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) }
                )
            }
        }

    internal val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .add(createSerializableAdapter<HistoryContextType>(HistoryContextType.UNKNOWN))
        .add(createSerializableAdapter<AlbumType>(AlbumType.ALBUM))
        .add(createSerializableAdapter<ReleaseDatePrecision>(ReleaseDatePrecision.YEAR))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_URL)
        .client(authenticatedHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    internal val api: SpotifyAPI = retrofit.create(SpotifyAPI::class.java)

    private inline fun <reified T> createSerializableAdapter(default: T): JsonAdapter<T> {
        return object : JsonAdapter<T>() {
            @FromJson
            override fun fromJson(reader: JsonReader): T {
                return if (reader.peek() != JsonReader.Token.NULL) {
                    try {
                        Json.decodeFromString<T>("\"${reader.nextString().lowercase()}\"")
                    } catch (throwable: Throwable) {
                        default
                    }
                } else {
                    default
                }
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: T?) {
                writer.value(Json.encodeToString(value).replace("\"", ""))
            }
        }
    }
}
