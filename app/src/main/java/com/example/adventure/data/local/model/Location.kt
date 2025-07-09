package com.example.adventure.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val locationKey: String,
    val latitude: Double,
    val longitude: Double
)