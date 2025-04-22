package com.example.adventure.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherConditionResponse(
    @SerializedName("LocalObservationDateTime") // ADD SerializedName
    val localObservationDateTime: String?,
    @SerializedName("EpochTime")
    val epochTime: Long?,
    @SerializedName("WeatherText")
    val weatherText: String?,
    @SerializedName("WeatherIcon")
    val weatherIcon: Int?,
    @SerializedName("LocalSource")
    val localSource: LocalSource?,
    @SerializedName("HasPrecipitation")
    val hasPrecipitation: Boolean?,
    @SerializedName("PrecipitationType")
    val precipitationType: String?,
    @SerializedName("IsDayTime")
    val isDayTime: Boolean?,
    @SerializedName("Temperature")
    val temperature: Temperature?,
    @SerializedName("MobileLink")
    val mobileLink: String?,
    @SerializedName("Link")
    val link: String?
)

@Serializable
data class LocalSource(
//    @SerializedName("ID")
//    val id: Int?,
    @SerializedName("Name")
    val name: String?,
    @SerializedName("WeatherCode")
    val weatherCode: String?
)

@Serializable
data class Temperature(
    @SerializedName("Metric")
    val metric: TemperatureUnit?,
    @SerializedName("Imperial")
    val imperial: TemperatureUnit?
)

@Serializable
data class TemperatureUnit(
    @SerializedName("Value")
    val value: Double?,
    @SerializedName("Unit")
    val unit: String?,
    @SerializedName("UnitType")
    val unitType: Int?
)