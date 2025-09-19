package com.example.adventure.api

import com.example.adventure.data.network.model.GeocodingResponse
import com.example.adventure.data.network.model.OpenWeatherResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Response<OpenWeatherResponseDTO>

    @GET("geo/1.0/direct")
    suspend fun getLocation(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingResponse>>

}