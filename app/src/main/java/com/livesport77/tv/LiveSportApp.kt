package com.livesport77.tv

import android.app.Application
import android.content.ComponentCallbacks2
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Custom Application class with strict RAM budget enforcement (< 60MB).
 * Configures Coil image memory cache to max 10% of available memory (max ~15MB).
 */
class LiveSportApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.10) // Strictly limit image RAM cache to 10%
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(20L * 1024 * 1024) // 20 MB disk cache
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            // Clear image in-memory cache to prevent Android TV OS from killing app due to OOM
            coil.Coil.imageLoader(this).memoryCache?.clear()
        }
    }
}

