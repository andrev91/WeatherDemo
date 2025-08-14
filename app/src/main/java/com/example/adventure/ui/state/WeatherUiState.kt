package com.example.adventure.ui.state

import androidx.compose.ui.text.input.TextFieldValue
import com.example.adventure.data.local.model.Bookmark
import com.example.adventure.data.model.State
import com.example.adventure.viewmodel.UnitType
import com.example.adventure.viewmodel.WeatherDisplayData

data class WeatherUiState(
    val locationState : LocationSelectionState = LocationSelectionState(),
    val weatherState : WeatherDataState = WeatherDataState(),
    val error: String? = null
)

data class LocationSelectionState(
    val isLoadingStates: Boolean = false,
    val availableStates: List<State>? = emptyList(),
    val selectedState: State? = null,
    val stateSearchQuery: TextFieldValue = TextFieldValue(""),
    val filteredStates: List<State> = emptyList(),
    val isLoadingCities: Boolean = false,
    val availableCities: List<String>? = emptyList(),
    val selectedCity: String? = null,
    val citySearchQuery: TextFieldValue = TextFieldValue(""),
    val filteredCities: List<String> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList()
)

data class WeatherDataState(
    val isLoadingWeather: Boolean = false,
    val displayData: WeatherDisplayData? = null,
    val temperatureUnit: UnitType = UnitType.CELSIUS
)