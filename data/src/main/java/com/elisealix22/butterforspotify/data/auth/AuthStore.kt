package com.elisealix22.butterforspotify.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elisealix22.butterforspotify.data.model.user.User
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

    private val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
    private val ACTIVE_ACCESS_TOKEN = stringPreferencesKey("active_access_token")
    private val ACTIVE_REFRESH_TOKEN = stringPreferencesKey("active_refresh_token")

    internal suspend fun setActiveUser(
        userId: String,
        accessToken: String,
        refreshToken: String
    ) {
        applicationContext.authStore.edit { store ->
            store[ACTIVE_USER_ID] = userId
            store[ACTIVE_ACCESS_TOKEN] = accessToken
            store[ACTIVE_REFRESH_TOKEN] = refreshToken
        }
    }

    internal suspend fun isUserSignedIn(user: User): Boolean {
        runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }?.let { store ->
            store[ACTIVE_USER_ID]?.let {
                return it.isNotBlank() && it == user.id
            }
        }
        return false
    }

    internal val activeUserId: String?
        get() = runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }?.let { store ->
            store[ACTIVE_USER_ID]
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
            store?.get(ACTIVE_USER_ID).orEmpty().isNotBlank() &&
                store?.get(ACTIVE_ACCESS_TOKEN).orEmpty().isNotBlank() &&
                store?.get(ACTIVE_REFRESH_TOKEN).orEmpty().isNotBlank()
        }

    val authenticatedFlow: Flow<Boolean>
        get() = applicationContext.authStore.data.map {
            it[ACTIVE_USER_ID].orEmpty().isNotBlank() &&
                it[ACTIVE_ACCESS_TOKEN].orEmpty().isNotBlank() &&
                it[ACTIVE_REFRESH_TOKEN].orEmpty().isNotBlank()
        }

    suspend fun clearActiveTokens() {
        applicationContext.authStore.edit { auth ->
            auth[ACTIVE_ACCESS_TOKEN] = ""
            auth[ACTIVE_REFRESH_TOKEN] = ""
            auth[ACTIVE_USER_ID] = ""
        }
    }
}
