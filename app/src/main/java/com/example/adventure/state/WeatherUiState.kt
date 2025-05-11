package com.example.adventure.state

import com.example.adventure.viewmodel.LocationDisplayData
import com.example.adventure.viewmodel.LocationOption
import com.example.adventure.viewmodel.UnitType
import com.example.adventure.viewmodel.WeatherDisplayData

data class WeatherUiState(
    val isLoadingWeatherData: Boolean = false,
    val isLoadingLocationData: Boolean = false,
    val isLoadingLocationList: Boolean = false,
    val weatherDisplayData: WeatherDisplayData? = null,
    val locationDisplayData: LocationDisplayData? = null,
    val availableLocations: List<LocationOption>? = null,
    val selectedLocation: LocationOption? = null,
    val error: String? = null,
    var temperatureUnit: UnitType = UnitType.CELSIUS
)
