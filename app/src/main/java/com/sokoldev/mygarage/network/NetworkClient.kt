package com.sokoldev.mygarage.network

import okhttp3.*
import java.io.IOException

class NetworkClient {
    private val client = OkHttpClient()

    fun makeRequest(url: String, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(callback)
    }
}
