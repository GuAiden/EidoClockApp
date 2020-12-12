package com.example.clockofeidolon.clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import java.time.*

@Serializable
data class ExpiryData(val expiryDate: Long)

class Clock {
    private val client = OkHttpClient()
    var lastSync: Long = 0
    var syncTimeout: Long = 60000
    var displayExpiryTime: Long = 0
    var expiryTime = 0
    var hasLoaded = false
    var syncing = false

    private fun syncTime() {
        if (syncing) return
        var currTime = Instant.now()
        if (currTime.toEpochMilli() - lastSync >= syncTimeout || expiryTime == 0) {
            syncing = true
            currTime = Instant.now()
            lastSync = currTime.toEpochMilli()

            // Make get request
            val request = Request.Builder()
                .url("https://us-central1-eidoclock.cloudfunctions.net/getTime")
                .build()
            val response = client.newCall(request).execute()

            // Evaluate response
            if (response.body == null) {
                println("Failed Api call")
            } else {
                try {
                    val jsonify = Json.decodeFromString<ExpiryData>("""${response.body?.string()}""")
                } catch (error: Exception) {
                    println("An error has occurred in deserialization")
                    println(error.stackTraceToString())
                }
            }
        }
    }
}