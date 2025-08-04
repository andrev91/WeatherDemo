package com.example.adventure.data.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class State(
    val name: String,
    @SerialName("code") val abbreviation: String
)
