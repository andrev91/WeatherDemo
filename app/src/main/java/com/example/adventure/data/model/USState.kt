package com.example.adventure.data.model

import kotlinx.serialization.Serializable

@Serializable
data class USState(
    val name: String,
    val code: String
)
