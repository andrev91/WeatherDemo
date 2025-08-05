package com.example.adventure.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherConditionResponse(
    @SerialName("LocalObservationDateTime") // ADD SerialName
    val localObservationDateTime: String?,
    @SerialName("EpochTime")
    val epochTime: Long?,
    @SerialName("WeatherText")
    val weatherText: String?,
    @SerialName("WeatherIcon")
    val weatherIcon: Int?,
    //As of 7/24/2025 - this data is not returning on the API.
    //ktx json throws error if it is not present
//    @SerialName("LocalSource")
//    val localSource: LocalSource?,
    @SerialName("HasPrecipitation")
    val hasPrecipitation: Boolean?,
    @SerialName("PrecipitationType")
    val precipitationType: String?,
    @SerialName("IsDayTime")
    val isDayTime: Boolean?,
    @SerialName("Temperature")
    val temperature: Temperature?,
    @SerialName("MobileLink")
    val mobileLink: String?,
    @SerialName("Link")
    val link: String?
)

@Serializable
data class LocalSource(
    @SerialName("ID")
    val id: Int?,
    @SerialName("Name")
    val name: String?,
    @SerialName("WeatherCode")
    val weatherCode: String?
)

@Serializable
data class Temperature(
    @SerialName("Metric")
    val metric: TemperatureUnit?,
    @SerialName("Imperial")
    val imperial: TemperatureUnit?
)

@Serializable
data class TemperatureUnit(
    @SerialName("Value")
    val value: Double?,
    @SerialName("Unit")
    val unit: String?,
    @SerialName("UnitType")
    val unitType: Int?
)