package com.example.adventure.state

import com.example.adventure.repository.LocationRepository
import com.example.adventure.viewmodel.UnitType
import com.example.adventure.viewmodel.WeatherDisplayData

data class WeatherUiState(
    val locationState : LocationSelectionState = LocationSelectionState(),
    val weatherState : WeatherDataState = WeatherDataState(),
    val error: String? = null
)

data class LocationSelectionState(
    val isLoadingStates: Boolean = false,
    val availableStates: List<LocationRepository.State>? = null,
    val selectedState: LocationRepository.State? = null,
    val stateSearchQuery: String = "",
    val filteredStates: List<LocationRepository.State> = emptyList(),
    val isLoadingCities: Boolean = false,
    val availableCities: List<String>? = null,
    val selectedCity: String? = null,
    val citySearchQuery: String = "",
    val filteredCities: List<String> = emptyList()
)

data class WeatherDataState(
    val isLoadingWeather: Boolean = false,
    val displayData: WeatherDisplayData? = null,
    val temperatureUnit: UnitType = UnitType.CELSIUS
)