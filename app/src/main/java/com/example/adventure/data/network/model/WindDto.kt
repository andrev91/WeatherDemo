package com.example.adventure.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class WindDto(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)
