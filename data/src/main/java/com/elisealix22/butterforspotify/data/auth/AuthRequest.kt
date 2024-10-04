package com.elisealix22.butterforspotify.data.auth

import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.squareup.moshi.adapter
import okhttp3.Request

@OptIn(ExperimentalStdlibApi::class)
@Throws(ServiceError::class)
internal fun Request.executeFetchTokens(): AuthResponse {
    val response = try {
        SpotifyClient.unAuthenticatedHttpClient.newCall(this@executeFetchTokens).execute()
    } catch (ex: Throwable) {
        throw ServiceError.IOError(ex.message)
    }
    val responseBody = response.body?.string().orEmpty()
    if (!response.isSuccessful) {
        val errorDescription = try {
            SpotifyClient.moshi.adapter<AuthError>().fromJson(responseBody)?.errorDescription
        } catch (ex: Throwable) {
            null
        }
        throw ServiceError.ApiError(
            code = response.code,
            loggingMessage = responseBody,
            userFriendlyMessage = errorDescription
        )
    }
    val authResponse = try {
        SpotifyClient.moshi.adapter<AuthResponse>().fromJson(responseBody)
    } catch (ex: Throwable) {
        throw ServiceError.UnexpectedResponseError(ex.message)
    } ?: throw ServiceError.UnexpectedResponseError("Empty auth response")
    return authResponse
}
