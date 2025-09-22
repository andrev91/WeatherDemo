package com.example.adventure.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val name: String,
    @SerialName("local_names")
    val localNames: Map<String, String>? = null,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)
