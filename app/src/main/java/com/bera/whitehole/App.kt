package com.bera.whitehole

import android.app.Application
import com.bera.whitehole.BuildConfig.POSTHOG_API_KEY
import com.bera.whitehole.BuildConfig.POSTHOG_HOST
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.coil.ImageLoaderModule
import com.bera.whitehole.data.localphotosource.LocalPhotoSource
import com.bera.whitehole.workers.WorkModule
import com.posthog.android.BuildConfig
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Preferences.init(applicationContext)
        DbHolder.create(applicationContext)
        WorkModule.create(applicationContext)
        ImageLoaderModule.create(applicationContext)
        LocalPhotoSource.create(applicationContext)
        BotApi.create()

        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST
        )
        PostHogAndroid.setup(this, config)
    }
}