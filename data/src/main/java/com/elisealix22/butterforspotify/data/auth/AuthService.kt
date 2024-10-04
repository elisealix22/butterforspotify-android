package com.elisealix22.butterforspotify.data.auth

import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Request

class AuthService {

    /**
     * Fetches an auth token and refresh token from the Spotify Web API.
     *
     * https://developer.spotify.com/documentation/web-api/tutorials/code-flow
     */
    @Throws(ServiceError::class)
    suspend fun fetchAuthToken(code: String): Flow<Boolean> = flow {
        val form = FormBody.Builder()
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", BuildConfig.SPOTIFY_REDIRECT_URI)
            .build()
        val request: Request = Request.Builder()
            .url(SpotifyClient.TOKEN_URL)
            .post(form)
            .header("content-type", "application/x-www-form-urlencoded")
            .header(
                "Authorization",
                Credentials.basic(
                    BuildConfig.SPOTIFY_CLIENT_ID,
                    BuildConfig.SPOTIFY_CLIENT_SECRET
                )
            )
            .build()
        val authResponse = request.executeFetchTokens()
        try {
            AuthStore.setActiveTokens(
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken.orEmpty()
            )
        } catch (ex: Throwable) {
            throw ServiceError.UnexpectedResponseError(ex.message)
        }
        if (AuthStore.isAuthenticated) {
            emit(true)
        } else {
            throw ServiceError.UnexpectedResponseError("Couldn't store tokens")
        }
    }
}
