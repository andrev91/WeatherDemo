package com.example.adventure.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherLocationResponse(
    @SerialName("Key")
    val key: String?,
    @SerialName("EnglishName")
    val englishName: String?,
    @SerialName("LocalizedName")
    val localizedName: String?,
    @SerialName("Region")
    val region: Region? = null,
    @SerialName("Country")
    val country: Country? = null,
    @SerialName("AdministrativeArea")
    val administrativeArea: AdministrativeArea? = null
)

@Serializable
data class Region(
    @SerialName("ID")
    val id: String?,
    @SerialName("LocalizedName")
    val localizedName: String?, // e.g., "North America"
    @SerialName("EnglishName")
    val englishName: String?
)

@Serializable
data class Country(
    @SerialName("ID")
    val id: String?, // e.g., "US"
    @SerialName("LocalizedName")
    val localizedName: String?, // e.g., "United States"
    @SerialName("EnglishName")
    val englishName: String?
)

@Serializable
data class AdministrativeArea(
    @SerialName("ID")
    val id: String?, // e.g., "NY"
    @SerialName("LocalizedName")
    val localizedName: String?, // e.g., "New York"
    @SerialName("EnglishName")
    val englishName: String?,
    @SerialName("Level")
    val level: Int?
)
