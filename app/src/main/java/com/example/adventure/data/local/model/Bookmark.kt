package com.example.adventure.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stateName: String,
    val stateAbbreviation: String,
    val cityName: String
)
