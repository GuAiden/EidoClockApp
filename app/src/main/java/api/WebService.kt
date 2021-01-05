package api

import retrofit2.Call
import retrofit2.http.GET

interface WebService {
    @GET("/getTime")
    fun getTime(): Call<ExpiryData>
}