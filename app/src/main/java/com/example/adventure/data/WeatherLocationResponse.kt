package com.example.adventure.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherLocationResponse(
    @SerializedName("Key")
    val key: String?,
    @SerializedName("LocalizedName")
    val localizedName: String?,
    @SerializedName("EnglishName")
    val englishName: String?,
    @SerializedName("Region")
    val region: Region?,
    @SerializedName("Country")
    val country: Country?,
    @SerializedName("AdministrativeArea")
    val administrativeArea: AdministrativeArea?
)

@Serializable
data class Region(
    @SerializedName("ID")
    val id: String?,
    @SerializedName("LocalizedName")
    val localizedName: String?, // e.g., "North America"
    @SerializedName("EnglishName")
    val englishName: String?
)

@Serializable
data class Country(
    @SerializedName("ID")
    val id: String?, // e.g., "US"
    @SerializedName("LocalizedName")
    val localizedName: String?, // e.g., "United States"
    @SerializedName("EnglishName")
    val englishName: String?
)

@Serializable
data class AdministrativeArea(
    @SerializedName("ID")
    val id: String?, // e.g., "NY"
    @SerializedName("LocalizedName")
    val localizedName: String?, // e.g., "New York"
    @SerializedName("EnglishName")
    val englishName: String?,
    @SerializedName("Level")
    val level: Int?
)
