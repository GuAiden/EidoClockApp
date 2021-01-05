package api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TimeRetriever {
    private val service: WebService

    companion object {
        const val BASE_URL = "https://us-central1-eidoclock.cloudfunctions.net/"
    }

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        service = retrofit.create(WebService::class.java)
    }

    fun retrieveTime(callback: (ExpiryData?) -> Unit) {
        val call = service.getTime()
        // call.enqueue(callback)

        call.enqueue(object : Callback<ExpiryData> {
            override fun onFailure(call: Call<ExpiryData>?, t: Throwable?) {
                Log.e("MainActivity", "Problem calling firebase")
                callback(null)
            }

            override fun onResponse(call: Call<ExpiryData>?, response: Response<ExpiryData>?) {
                response?.isSuccessful.let {
                    val fetchedTime = ExpiryData(response?.body()?.expiryDate ?: 0)
                    callback(fetchedTime)
                }
            }
        })

    }

}