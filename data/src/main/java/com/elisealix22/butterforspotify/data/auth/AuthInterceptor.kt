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
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer ${AuthStore.activeAccessToken}")
            .build()
        val response = chain.proceed(request)

        if (!response.isSuccessful && response.code == 401) {
            Log.i(TAG, "Refreshing auth token")
            val form = FormBody.Builder()
                .add("refresh_token", AuthStore.activeRefreshToken.orEmpty())
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
                Log.e(TAG, "Error fetching refresh token")
                runBlocking { AuthStore.clearActiveTokens() }
                return response
            }
            if (authResponse.accessToken.isBlank()) {
                Log.e(TAG, "Invalid auth token")
                runBlocking { AuthStore.clearActiveTokens() }
                return response
            }
            val newRefreshToken = authResponse.refreshToken.orEmpty().let {
                // Use the existing refresh token if one is not returned from the API.
                it.ifBlank { AuthStore.activeRefreshToken.orEmpty() }
            }
            runBlocking {
                AuthStore.setActiveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = newRefreshToken
                )
            }
            if (AuthStore.isAuthenticated) {
                Log.i(TAG, "Successfully authenticated. Creating new request.")
                response.close()
                val retryOriginalRequest = request
                    .newBuilder()
                    .header("Authorization", "Bearer ${authResponse.accessToken}")
                    .build()
                return chain.proceed(retryOriginalRequest)
            } else {
                Log.e(TAG, "Couldn't store new tokens")
                runBlocking { AuthStore.clearActiveTokens() }
                return response
            }
        }
        return response
    }
}
