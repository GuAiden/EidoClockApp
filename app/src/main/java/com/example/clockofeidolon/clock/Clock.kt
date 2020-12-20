package com.example.clockofeidolon.clock
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.*
import java.time.*
import kotlin.math.floor

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


    /**
     * Syncs the expiry time by making
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
                    val body = response.body?.string()
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

    /**
     * @return when the current plains cycle expires
     */
    private suspend fun getNextExpiryTime(): Long {

        withContext(Dispatchers.IO) {
            try {
                syncTime()
                delay(1_000)
            } catch (cause: Throwable) {
                throw Error("Unable to make get request", cause)
            }

            // If the current time is past the expiry time, update displayExpiryTime
            displayExpiryTime = if (Instant.now().toEpochMilli() >= expiryTime) {
                expiryTime + 150 * 60 * 1000
            } else {
                expiryTime
            }
        }

        return displayExpiryTime
//        syncTime()
//        // If the current time is past the expiry time, update displayExpiryTime
//        displayExpiryTime = if (Instant.now().toEpochMilli() >= expiryTime) {
//            expiryTime + 150 * 60 * 1000
//        } else {
//            expiryTime
//        }
//        return displayExpiryTime
    }

    /**
     * @return time difference from the current time to the next day cycle instance
     */
    private suspend fun getTimeUntilDay(): Long {
        return getNextExpiryTime() - Instant.now().toEpochMilli()
    }

    /**
     * @return the time til the next event cycle, day/night
     */
    suspend fun getTimeUntilNextEvent(): Long {
        var dayTime = getTimeUntilDay()
        val nightTime = 50 * 60 * 1000
        // If the time left is greater than the night period
        // return night event timer
        if (dayTime > nightTime) {
            return dayTime - nightTime
        }
        return dayTime
    }

    /**
     * Formats the utc timestamp to next event as a
     * hours:minutes:seconds string
     * @param timestamp given utc timestamp
     * @return the formatted string representing the timestamp
     */
    private fun utcToHMS(timestamp: Long): String {
        val totalSeconds = timestamp / 1000
        val totalMinutes = totalSeconds / 60;
        val hours = formatTime(floor((totalMinutes / 60).toDouble()))
        val minutes = formatTime(floor((totalMinutes % 60).toDouble()))
        val seconds = formatTime(floor((totalSeconds % 60).toDouble()))
        return "${hours}:${minutes}:${seconds}"
    }

    /**
     * Pads the time if its less than 0
     */
    private fun formatTime(num: Double): String {
        if (num < 10) {
            return "0${num.toInt()}"
        }
        return "${num.toInt()}"
    }

    /**
     * @return if its currently night
     */
    suspend fun isNight(): Boolean {
        if (!hasLoaded) return false
        var dayTime = getTimeUntilDay()
        var nightTime = 50 * 60 * 1000
        if (dayTime > nightTime) {
            return false
        }
        return true
    }

    /**
     * @return when next event occurs as hours:minutes:seconds
     */
    suspend fun getEventTime(): String {
        var eventTime = getTimeUntilNextEvent()
        return utcToHMS(eventTime)
    }
}