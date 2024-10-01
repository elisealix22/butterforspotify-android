package com.elisealix22.butterforspotify.android

import android.app.Application
import com.elisealix22.butterforspotify.data.auth.AuthStore

class ButterApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AuthStore.init(applicationContext = this)
    }
}
