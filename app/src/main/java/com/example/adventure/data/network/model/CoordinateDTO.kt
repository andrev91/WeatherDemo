package com.example.adventure.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class CoordinateDTO(
    val lon: Double,
    val lat: Double
)
