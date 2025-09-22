package com.example.adventure.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherDetailsDTO(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
