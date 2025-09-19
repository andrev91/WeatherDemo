package com.example.adventure.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherResponseDTO(
    val coord: CoordinateDTO,
    val weather: List<WeatherDetailsDTO>,
    val base: String,
    @SerialName("main")
    val main: WeatherObjectDTO,
    val visibility: Int,
    val wind: WindDTO,
    val rain: RainDurationDTO? = null,
    val clouds: CloudsDTO,
    val dt: Long,
    val sys: WeatherLocationDTO,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)
