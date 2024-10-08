package com.elisealix22.butterforspotify.data.auth

import android.util.Log
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.SpotifyClient.TOKEN_URL
import com.elisealix22.butterforspotify.data.error.ServiceError
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class AuthInterceptor : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = AuthStore.activeAccessToken.orEmpty()
        val refreshToken = AuthStore.activeRefreshToken.orEmpty()

        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        val response = chain.proceed(request)

        if (!response.isSuccessful && response.code == 401) {
            Log.i(TAG, "Refreshing auth token")
            val form = FormBody.Builder()
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build()
            val refreshAuthRequest = Request.Builder()
                .url(TOKEN_URL)
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
            val authResponse = try {
                refreshAuthRequest.executeFetchTokens()
            } catch (ex: ServiceError) {
                return response.handleAuthError("Error fetching refresh token")
            }
            val newAccessToken = authResponse.accessToken
            if (newAccessToken.isBlank()) {
                return response.handleAuthError("Invalid auth token")
            }
            val newRefreshToken = authResponse.refreshToken.orEmpty().let {
                // Use the existing refresh token if one is not returned from the API.
                it.ifBlank { refreshToken }
            }
            runBlocking {
                AuthStore.setActiveTokens(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )
            }
            if (AuthStore.isAuthenticated) {
                Log.i(TAG, "Successfully authenticated. Creating new request.")
                response.close()
                val retryOriginalRequest = request
                    .newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                logDebugAccessToken(newAccessToken)
                return chain.proceed(retryOriginalRequest)
            } else {
                return response.handleAuthError("Couldn't store new tokens")
            }
        }

        logDebugAccessToken(accessToken)

        return response
    }

    private fun logDebugAccessToken(token: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Bearer $token")
        }
    }

    private fun Response.handleAuthError(errorMessage: String) : Response {
        Log.e(TAG, errorMessage)
        runBlocking { AuthStore.clearActiveTokens() }
        return this
    }
}
