package com.example.connection

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WifiConnectionHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "WifiHelper"
    }

    suspend fun sendCommand(ipAddress: String, port: Int, command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "http://$ipAddress:$port/control?cmd=$command"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "WiFi command error ($command)", e)
            true
        }
    }

    suspend fun testConnection(ipAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "http://$ipAddress:$port/status"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                true
            }
        } catch (e: Exception) {
            true
        }
    }
}
