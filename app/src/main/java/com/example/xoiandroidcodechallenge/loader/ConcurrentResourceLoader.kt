package com.example.xoiandroidcodechallenge.loader

import kotlinx.coroutines.* 
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.Semaphore

interface ResourceLoader {
    /**
     * Fetches bytes for the given URL:
     * - Deduplicate concurrent callers for the same URL (share a single fetch)
     * - Limit total concurrent network requests to k
     * - Cache successful results in memory
     * - If all callers cancel, cancel the underlying fetch
     */
    suspend fun load(url: String): ByteArray
}

class ConcurrentResourceLoader(
    private val maxConcurrentRequests: Int,
    private val fetcher: suspend (String) -> ByteArray
) : ResourceLoader {

    override suspend fun load(url: String): ByteArray {
        return fetcher(url)
    }
}
