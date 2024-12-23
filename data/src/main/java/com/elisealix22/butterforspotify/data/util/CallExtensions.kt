package com.elisealix22.butterforspotify.data.util

import android.util.Log
import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ErrorResponse
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.squareup.moshi.adapter
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call

@OptIn(ExperimentalStdlibApi::class)
@Throws(ServiceError::class)
internal suspend fun <T> Call<T>.fetchFromNetwork(): Flow<T> = flow {
    val response = try {
        execute()
    } catch (ex: Throwable) {
        Log.e("fetchFromNetwork", "Error executing call", ex)
        when (ex) {
            is IOException -> throw ServiceError.IOError(ex.message)
            else -> throw ServiceError.UnexpectedResponseError(ex.message)
        }
    }
    if (!response.isSuccessful) {
        val errorBody = response.errorBody()?.string().orEmpty()
        val userFriendlyMessage = try {
            SpotifyClient.moshi.adapter<ErrorResponse>()
                .fromJson(errorBody)
                ?.error?.message
        } catch (ex: Throwable) {
            null
        }
        throw ServiceError.ApiError(
            code = response.code(),
            userFriendlyMessage = userFriendlyMessage,
            loggingMessage = errorBody
        )
    }
    val parsedResponse = response.body()
        ?: throw ServiceError.UnexpectedResponseError("Missing response body")
    emit(parsedResponse)
}
