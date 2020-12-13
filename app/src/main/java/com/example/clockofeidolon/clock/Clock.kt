package com.example.clockofeidolon.clock
import com.google.gson.GsonBuilder
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
    var expiryTime: Long = 0
    var hasLoaded = false
    var syncing = false

    init {
        syncTime()
    }

    /**
     * A function that syncs the expiry time by making
     * a get request to firebase to update the time
     */
    private fun syncTime() {
        //
        if (syncing) return
        var currTime = Instant.now()
        if (currTime.toEpochMilli() - lastSync >= syncTimeout || expiryTime == 0.toLong()) {
            syncing = true
            currTime = Instant.now()
            lastSync = currTime.toEpochMilli()

            // Make get request
            val request = Request.Builder()
                .url("https://us-central1-eidoclock.cloudfunctions.net/getTime")
                .build()
            val response = client.newCall(request).execute()

            // Evaluate response, deserialize payload and update expiry time.
            if (response.body == null) {
                println("Failed Api call")
            } else {
                try {
                    val gson = GsonBuilder().create()
                    val body = response?.body?.string()
                    val data = gson.fromJson(body, ExpiryData::class.java)
                    if (expiryTime != data.expiryDate) {
                        expiryTime = data.expiryDate
                        hasLoaded = true
                    }
                    syncing = false
                } catch (error: Exception) {
                    println("An error has occurred in deserialization")
                    println(error.stackTraceToString())
                }
            }
        }
    }

    fun getNextExpiryTime(): Long {
        syncTime()
        // If the current time is past the expiry time, update displayExpiryTime
        if (Instant.now().toEpochMilli() >= expiryTime) {
            displayExpiryTime = expiryTime + 150 * 60 * 1000
        } else {
            displayExpiryTime = expiryTime
        }
        return displayExpiryTime
    }

    /**
     * Gets the time difference from the current time
     * to the next day cycle instance
     */
    private fun getTimeUntilDay(): Long {
        return getNextExpiryTime() - Instant.now().toEpochMilli()
    }

    /**
     * Gets the time til the next event cycle, i.e. day/dusk
     */
    fun getTimeUntilNextEvent(): Long {
        var dayTime = getTimeUntilDay()
        val nightTime = 50 * 60 * 1000
        // If the time left is greater than the night period
        // return night event timer
        if (dayTime > nightTime) {
            return dayTime - nightTime
        }
        return dayTime
    }
}