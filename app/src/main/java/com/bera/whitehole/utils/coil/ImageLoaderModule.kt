package com.bera.whitehole.utils.coil

import android.content.Context
import coil.ImageLoader
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger

object ImageLoaderModule {
    lateinit var remoteImageLoader: ImageLoader
    lateinit var defaultImageLoader: ImageLoader
    fun create(appContext: Context) {
        defaultImageLoader = appContext.imageLoader
        remoteImageLoader = ImageLoader.Builder(appContext)
            .crossfade(true)
            .respectCacheHeaders(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.2)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .components { add(NetworkFetcher.Factory()) }
            .logger(DebugLogger())
            .build()
    }
}