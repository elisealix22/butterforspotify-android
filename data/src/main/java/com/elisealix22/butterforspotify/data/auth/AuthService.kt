package com.elisealix22.butterforspotify.data.auth

import android.util.Log
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.SpotifyClient
import com.elisealix22.butterforspotify.data.error.ServiceError
import com.elisealix22.butterforspotify.data.model.user.User
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Request

class AuthService {

    companion object {
        private const val TAG = "AuthService"
    }

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
        val user = fetchUserForToken(authResponse.accessToken).firstOrNull()
        try {
            AuthStore.setActiveUser(
                userId = user?.id.orEmpty(),
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken.orEmpty()
            )
        } catch (ex: Throwable) {
            throw ServiceError.UnexpectedResponseError(ex.message)
        }
        if (AuthStore.isAuthenticated) {
            emit(true)
        } else {
            throw ServiceError.UnexpectedResponseError("Unauthenticated")
        }
    }

    @Throws(ServiceError::class)
    suspend fun verifyAppRemoteUserSignedIn(appRemoteToken: String): Flow<User?> =
        fetchUserForToken(appRemoteToken)
            .flowOn(Dispatchers.IO)
            .catch { error ->
                Log.e(TAG, "Error verifying app remote token", error)
                if (error !is ServiceError.IOError) {
                    AuthStore.clearActiveTokens()
                }
            }
            .map { user ->
                val signedIn = AuthStore.isUserSignedIn(user)
                if (signedIn) {
                    Log.e(TAG, "App remote user is authenticated.")
                    user
                } else {
                    Log.e(TAG, "App remote user is not signed in. Clearing tokens.")
                    AuthStore.clearActiveTokens()
                    null
                }
            }

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ServiceError::class)
    private suspend fun fetchUserForToken(appRemoteToken: String): Flow<User> = flow {
        val request: Request = Request.Builder()
            .url("${SpotifyClient.API_URL}me")
            .header("Authorization", "Bearer $appRemoteToken")
            .get()
            .build()
        val response = try {
            SpotifyClient.unAuthenticatedHttpClient
                .newCall(request)
                .execute()
        } catch (ex: Throwable) {
            throw ServiceError.IOError(ex.message)
        }
        val responseBody = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw ServiceError.ApiError(
                code = response.code,
                loggingMessage = responseBody,
                userFriendlyMessage = null
            )
        }
        val user = try {
            SpotifyClient.moshi.adapter<User>().fromJson(responseBody)
        } catch (ex: Throwable) {
            throw ServiceError.UnexpectedResponseError(ex.message)
        } ?: throw ServiceError.UnexpectedResponseError("Error verifying user")
        emit(user)
    }
}
