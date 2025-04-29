package com.example.adventure.api

import com.example.adventure.data.WeatherConditionResponse
import com.example.adventure.data.WeatherLocationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("currentconditions/v1/{locationKey}")
    suspend fun getWeather(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String,
        @Query("details") details: Boolean = true
    ) : Response<List<WeatherConditionResponse>>

    @GET("locations/v1/{locationKey}")
    suspend fun getLocation(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apiKey: String
    ) : Response<WeatherLocationResponse>

    @GET("locations/v1/adminareas/{countryCode}")
    suspend fun getUnitedStatesLocations(
        @Path("countryCode") countryCode: String = "US",
        @Query("apikey") apiKey: String
    ) : Response<List<WeatherLocationResponse>>

    @GET("locations/v1/search")
    suspend fun searchLocation(
        @Query("q") query: String = "",
        @Query("apikey") apiKey: String
    ) : Response<List<WeatherLocationResponse>>

}