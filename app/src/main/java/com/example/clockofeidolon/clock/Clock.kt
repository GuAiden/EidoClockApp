package com.example.clockofeidolon.clock
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import api.ExpiryData
import api.TimeRetriever
import api.WebService
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import kotlin.math.floor

class Clock {
    private val client = OkHttpClient()
    var lastSync: Long = 0
    var syncTimeout: Long = 60000
    var displayExpiryTime: Long = 0
    var expiryTime: Long = 0
    var hasLoaded = false
    var syncing = false

    private fun findTime() {
        // TODO: Test network connection before making network call
    }

//    private fun retrieveTime() {
//        val mainActivityJob = Job()
//        // TODO: Handle Exceptions
//        val coroutineScope = CoroutineScope(mainActivityJob + Dispatchers.Main)
//        coroutineScope.launch() {
//            val expiryData = TimeRetriever().retrieveTime()
//            expiryTime = expiryData.expiryDate
//        }
//    }

    fun apiCallback(expiryData: ExpiryData?) {
        if (expiryData == null) {
            println("Api call failed")
        } else {
            this.expiryTime = expiryData.expiryDate
            println(expiryData.expiryDate)
        }
    }

    private fun retrieveTime() {
//        val callback = object : Callback<ExpiryData> {
//            override fun onFailure(call: Call<ExpiryData>?, t: Throwable?) {
//                Log.e("MainActivity", "Problem calling firebase")
//            }
//
//            override fun onResponse(call: Call<ExpiryData>?, response:Response<ExpiryData>?) {
//                response?.isSuccessful.let {
//                    val fetchedTime = ExpiryData(response?.body()?.expiryDate ?: 0)
//                    println(fetchedTime)
//                    expiryTime = fetchedTime.expiryDate
//                    println(expiryTime)
//                    callback(fetchedTime)
//                }
//            }
//        }
        TimeRetriever().retrieveTime(::apiCallback)
    }

    private fun syncTime() {
        //
        if (syncing) return
        var currTime = Instant.now()
        if (currTime.toEpochMilli() - lastSync >= syncTimeout || expiryTime == 0.toLong()) {
            syncing = true
            currTime = Instant.now()
            lastSync = currTime.toEpochMilli()

            // Make get request
            val lastExpiryTime = expiryTime
            retrieveTime()
            syncing = false
        }
    }
    /**
     * @return when the current plains cycle expires
     */
    private fun getNextExpiryTime(): Long {
            // If the current time is past the expiry time, update displayExpiryTime
        syncTime()
        displayExpiryTime = if (Instant.now().toEpochMilli() >= expiryTime) {
            expiryTime + 150 * 60 * 1000
        } else {
            expiryTime
        }
        return displayExpiryTime
    }


    /**
     * @return time difference from the current time to the next day cycle instance
     */
    private fun getTimeUntilDay(): Long {
       return getNextExpiryTime() - Instant.now().toEpochMilli()
    }

    /**
     * @return the time til the next event cycle, day/night
     */
    private fun getTimeUntilNextEvent(): Long {
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
    fun isNight(): Boolean {
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
    fun getEventTime(): String {
        var eventTime = getTimeUntilNextEvent()
        return utcToHMS(eventTime)
    }
}