package com.example.adventure.data.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class StateCities (
    @SerialName("major_cities") val majorCities: List<String> = emptyList(),
    @SerialName("all_cities") val allCities: List<String> = emptyList()
)