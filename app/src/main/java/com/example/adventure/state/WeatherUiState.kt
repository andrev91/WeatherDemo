package com.example.adventure.state

import com.example.adventure.repository.LocationRepository
import com.example.adventure.viewmodel.UnitType
import com.example.adventure.viewmodel.WeatherDisplayData

data class WeatherUiState(
    val isLoadingWeatherData: Boolean = false,
    val isLoadingStateList: Boolean = false,
    val isLoadingCityList: Boolean = false,
    val weatherDisplayData: WeatherDisplayData? = null,
    val availableStates: List<LocationRepository.State>? = null,
    val availableCities: List<String>? = null,
    val selectedState: LocationRepository.State? = null,
    val selectedCity: String? = null,
    val error: String? = null,
    var temperatureUnit: UnitType = UnitType.CELSIUS
)
