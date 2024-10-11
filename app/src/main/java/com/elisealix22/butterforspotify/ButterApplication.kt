package com.elisealix22.butterforspotify

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.elisealix22.butterforspotify.data.auth.AuthStore

class ButterApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        AuthStore.init(applicationContext = this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}
