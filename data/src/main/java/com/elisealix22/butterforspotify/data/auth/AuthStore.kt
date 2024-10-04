package com.elisealix22.butterforspotify.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object AuthStore {

    private lateinit var applicationContext: Context

    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
    }

    private val Context.authStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

    private val ACTIVE_ACCESS_TOKEN = stringPreferencesKey("active_access_token")
    private val ACTIVE_REFRESH_TOKEN = stringPreferencesKey("active_refresh_token")

    internal suspend fun setActiveTokens(accessToken: String, refreshToken: String) {
        applicationContext.authStore.edit { auth ->
            auth[ACTIVE_ACCESS_TOKEN] = accessToken
            auth[ACTIVE_REFRESH_TOKEN] = refreshToken
        }
    }

    internal val activeAccessToken: String?
        get() = runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }?.let { store ->
            store[ACTIVE_ACCESS_TOKEN]
        }

    internal val activeRefreshToken: String?
        get() = runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }?.let { store ->
            store[ACTIVE_REFRESH_TOKEN]
        }

    val isAuthenticated: Boolean
        get() = runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }.let { store ->
            store?.get(ACTIVE_ACCESS_TOKEN).orEmpty().isNotBlank() &&
            store?.get(ACTIVE_REFRESH_TOKEN).orEmpty().isNotBlank()
        }

    val authenticatedFlow: Flow<Boolean>
        get() = applicationContext.authStore.data.map {
            it[ACTIVE_ACCESS_TOKEN].orEmpty().isNotBlank() &&
                    it[ACTIVE_REFRESH_TOKEN].orEmpty().isNotBlank()
        }

    suspend fun clearActiveTokens() {
        applicationContext.authStore.edit { auth ->
            auth[ACTIVE_ACCESS_TOKEN] = ""
            auth[ACTIVE_REFRESH_TOKEN] = ""
        }
    }
}
