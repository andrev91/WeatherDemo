package com.example.adventure.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherResponseDto(
    val coord: CoordDto,
    val weather: List<WeatherDto>,
    val base: String,
    val main: MainDto,
    val visibility: Int,
    val wind: WindDto,
    val rain: RainDto? = null,
    val clouds: CloudsDto,
    val dt: Long,
    val sys: SysDto,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)
