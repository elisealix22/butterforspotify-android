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

    private val ACTIVE_USER_TOKEN = stringPreferencesKey("active_user_token")

    suspend fun setActiveUserToken(token: String) {
        applicationContext.authStore.edit { auth ->
            auth[ACTIVE_USER_TOKEN] = token
        }
    }

    val activeUserToken: Flow<String?>
        get() = applicationContext.authStore.data
            .map { preferences ->
                preferences[ACTIVE_USER_TOKEN]
            }

    val blockingActiveUserToken: String?
        get() = runBlocking {
            applicationContext.authStore.data.firstOrNull()
        }?.get(ACTIVE_USER_TOKEN)
}
