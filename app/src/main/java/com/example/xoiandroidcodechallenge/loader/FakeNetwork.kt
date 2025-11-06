package com.example.xoiandroidcodechallenge.loader

import kotlinx.coroutines.delay
import kotlin.random.Random

object FakeNetwork {
    suspend fun fetch(url: String): ByteArray {
        delay(500 + Random.nextLong(1000))
        return "bytes($url)".encodeToByteArray()
    }
}
