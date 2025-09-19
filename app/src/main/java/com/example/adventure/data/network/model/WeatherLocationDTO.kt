package com.example.adventure.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherLocationDTO(
    val type: Int? = null,
    val id: Int? = null,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)
