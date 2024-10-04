package com.elisealix22.butterforspotify

import android.app.Application
import com.elisealix22.butterforspotify.data.auth.AuthStore

class ButterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AuthStore.init(applicationContext = this)
    }
}
