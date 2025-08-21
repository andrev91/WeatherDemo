package com.example.adventure.viewmodel

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.adventure.data.local.model.Bookmark
import com.example.adventure.data.network.model.WeatherConditionResponse
import com.example.adventure.data.repository.LocationRepository
import com.example.adventure.ui.state.BookmarkState
import com.example.adventure.ui.state.LocationSelectionState
import com.example.adventure.ui.state.LocationType
import com.example.adventure.ui.state.LocationType.*
import com.example.adventure.ui.state.WeatherDataState
import com.example.adventure.ui.state.WeatherUiState
import com.example.adventure.util.WeatherIconMapper
import com.example.adventure.worker.WeatherWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

data class WeatherDisplayData(
    val temperatureFahrenheit : String,
    val temperatureCelsius : String,
    val weatherDescription: String,
    val weatherIcon: Int? = null,
    val observedAt : String,
)

enum class UnitType {
    CELSIUS, FAHRENHEIT;
    override fun toString(): String {
        return when (this) {
            CELSIUS -> "Celsius"
            FAHRENHEIT -> "Fahrenheit"
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val locationRepository: LocationRepository
    ) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _bookmarkStateChannel = Channel<BookmarkState>()
    val bookmarkStateChannel = _bookmarkStateChannel.receiveAsFlow()

    private var weatherWorkerUId: UUID? = null

    private val locationKey = "225007" //default location

    init {
        fetchStateList()
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            locationRepository.getBookmarks().collect { bookmarks ->
                updateLocationState { it.copy(bookmarks = bookmarks) }
            }
        }
    }

    fun addBookmark() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val state = _uiState.value.locationState.selectedState ?: return@withContext
                val city = _uiState.value.locationState.selectedCity ?: return@withContext

                if (locationRepository.isBookmarkDuplicate(state.name, city)) {
                    _bookmarkStateChannel.send(BookmarkState.onError("Cannot save duplicate Bookmark!"))
                    return@withContext
                }

                val bookmark = Bookmark(
                    stateName = state.name,
                    stateAbbreviation = state.abbreviation,
                    cityName = city
                )
                locationRepository.addBookmark(bookmark)
                _bookmarkStateChannel.send(BookmarkState.onSuccess("Bookmark successfully added!"))
            }
        }
    }

    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            locationRepository.removeBookmark(bookmark)
            _bookmarkStateChannel.send(BookmarkState.onDelete("Bookmark removed."))
        }
    }

    fun loadBookmark(bookmark: Bookmark) {
        setDropdownSelection(STATE, bookmark.stateName)
        setDropdownSelection(CITY, bookmark.cityName)
    }

    private fun searchStateList(query: TextFieldValue) {
        _uiState.update { it.copy(error = null) }
        updateLocationState { currentState ->
            val filteredStates = if (query.text.isBlank()) { currentState.availableStates }
            else {
                currentState.availableStates?.filter { state ->
                    state.name.contains(query.text, ignoreCase = true)
                } ?: emptyList()
            }
            currentState.copy(stateSearchQuery = query, filteredStates = filteredStates!!)
        }
    }

    fun clearDropdownSelection(locationType : LocationType) {
        when (locationType) {
            STATE -> {
                updateLocationState { currentState -> currentState.copy(selectedState = null, isLoadingStates = false, filteredStates = emptyList(),
                    isLoadingCities = false, selectedCity = null, availableCities = null,
                    stateSearchQuery = TextFieldValue(""), citySearchQuery = TextFieldValue("")) }
            }
            CITY -> {
                updateLocationState { currentState -> currentState.copy(
                    isLoadingCities = false, selectedCity = null, filteredCities = emptyList(), citySearchQuery = TextFieldValue("")) }
            }
        }
        updateWeatherState { currentState -> currentState.copy(displayData = null) }
        _uiState.update { it.copy(error = null) }
    }

    fun setDropdownSelection(locationType : LocationType, location: String) {
        when (locationType) {
            STATE -> {
                val state = locationRepository.getStateFromString(location) ?: return
                if (state == _uiState.value.locationState.selectedState) return

                updateLocationState { currentState -> currentState.copy(selectedState = state, isLoadingStates = false
                    , isLoadingCities = true, selectedCity = null, availableCities = null,
                    stateSearchQuery = TextFieldValue(state.name), citySearchQuery = TextFieldValue("")) }
                updateWeatherState { currentState -> currentState.copy(displayData = null) }
                _uiState.update { it.copy(error = null) }

                viewModelScope.launch {
                    val cities = locationRepository.getMajorCitiesByState(state.abbreviation)
                    updateLocationState { currentState -> currentState.copy(availableCities = cities, isLoadingCities = false) }
                }
            }
            CITY -> {
                if (location == _uiState.value.locationState.selectedCity) return
                updateLocationState { currentState -> currentState.copy(selectedCity = location, citySearchQuery = TextFieldValue(location)) }
                updateWeatherState { currentState -> currentState.copy(displayData = null) }
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun searchDropdownList(locationType: LocationType, query: TextFieldValue) {
        when (locationType) {
            STATE -> {
                searchStateList(query)
            }
            CITY -> {
                searchCityList(query)
            }
        }
    }

    private fun searchCityList(query: TextFieldValue) {
        if (_uiState.value.locationState.citySearchQuery == query) return
        updateLocationState { currentState ->
            _uiState.update { it.copy(error = null) }
            val filteredCities = if (query.text.isBlank()) {
                currentState.availableCities
            } else {
                val test = _uiState.value.locationState.selectedState?.abbreviation
                val cities = locationRepository.getCities()[test]
                cities!!.allCities
                    .filter { city ->
                        city.contains(query.text, ignoreCase = true)
                    }
            }
        currentState.copy(citySearchQuery = query, filteredCities = filteredCities!!) }
    }

    private fun fetchWeather(locationKey: String = "") {
        updateWeatherState { currentState -> currentState.copy(displayData = null) }
        _uiState.update { it.copy(error = null) }
        val weatherRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInputData(
                workDataOf(WeatherWorker.WEATHER_KEY to locationKey.ifEmpty { this.locationKey })
            ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        weatherWorkerUId = weatherRequest.id
        observerWeatherWork(weatherWorkerUId!!)
        workManager.enqueueUniqueWork(
            "WeatherLocation_$locationKey",
            ExistingWorkPolicy.REPLACE,
            weatherRequest)
    }

    fun searchLocation() {
        if (_uiState.value.weatherState.isLoadingWeather) return
        updateWeatherState { currentState -> currentState.copy(displayData = null, isLoadingWeather = true) }
        _uiState.update { it.copy(error = null) }
        val state = uiState.value.locationState.selectedState?.name ?: return
        val city = uiState.value.locationState.selectedCity ?: ""

        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                locationRepository.getOrFetchLocation("$state,$city")
                    .collect { result ->
                        result.onSuccess { location ->
                            fetchWeather(location.locationKey)
                        }.onFailure { error ->
                            _uiState.update { it.copy(error = error.message) }
                        }
                    }
            }
        }
    }

    fun triggerTempTypeChange(UName: UnitType) {
        if (_uiState.value.weatherState.temperatureUnit == UName) return
        updateWeatherState { currentState -> currentState.copy(temperatureUnit = UName) }
    }

    private fun fetchStateList() {
        if (_uiState.value.locationState.isLoadingStates) return

        updateLocationState { currentState -> currentState.copy(isLoadingStates = true, selectedState = null,
            selectedCity = null, availableStates = null, availableCities = null) }
        _uiState.update { it.copy(error = null) }
        val stateData = locationRepository.getStates()
        updateLocationState { currentState -> currentState.copy(availableStates = stateData, filteredStates = stateData,
            isLoadingStates = false) }
    }

    private fun observerWeatherWork(uuid: UUID) {
        workManager.getWorkInfoByIdFlow(uuid)
            .filterNotNull()
            .filter { it.id == weatherWorkerUId }
            .onEach { workInfo -> processWeather(workInfo) }
            .launchIn(viewModelScope)
    }

    private fun processWeather(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                val outputData = workInfo.outputData
                val success = outputData.getBoolean(WeatherWorker.OUTPUT_SUCCESS, false)

                if (success) {
                    val weatherJson = outputData.getString(WeatherWorker.WEATHER_JSON)
                    if (weatherJson != null) {
                        try {
                            // --- Parse JSON from Worker Output ---
                            val response = Json.decodeFromString<WeatherConditionResponse>(weatherJson)
                            updateWeatherState { currentState ->
                                currentState.copy(displayData = mapResponseToDisplayData(response),
                                    isLoadingWeather = false)
                            }
                            _uiState.update { it.copy(error = null) }
                            Log.d(TAG,"Successfully parsed weather data from worker.")
                        } catch (e: Exception) {
                            Log.e(TAG,"Error parsing JSON from worker output",e)
                            updateWeatherState { currentState -> currentState.copy(isLoadingWeather = false) }
                            _uiState.update { it.copy(error = "Failed to parse weather data.") }
                        }
                    } else {
                        Log.e(TAG, "Work succeeded but weather JSON was null.")
                        updateWeatherState { currentState -> currentState.copy(isLoadingWeather = false) }
                        _uiState.update { it.copy(error = "Received empty success response.") }
                    }
                } else {
                    // Worker finished but reported internal failure
                    val errorMsg = outputData.getString(WeatherWorker.OUTPUT_ERROR_MESSAGE)
                        ?: "Worker reported failure."
                    Log.e(
                        TAG,
                        "Work succeeded but internal flag was false: $errorMsg"
                    )
                    updateWeatherState { currentState -> currentState.copy(isLoadingWeather = false) }
                    _uiState.update { it.copy(error = errorMsg) }
                }
                weatherWorkerUId = null // Reset tracked ID once finished
            }

            WorkInfo.State.FAILED -> {
                val errorMsg = workInfo.outputData.getString(WeatherWorker.OUTPUT_ERROR_MESSAGE)
                    ?: "Unknown error"
                Log.e(TAG, "Work failed: $errorMsg")
                updateWeatherState { currentState -> currentState.copy(isLoadingWeather = false) }
                _uiState.update { it.copy(error = errorMsg) }
                weatherWorkerUId = null
            }

            WorkInfo.State.CANCELLED -> {
                Log.w(TAG, "Work cancelled.")
                _uiState.update {
                    _uiState.value.copy(error = "Weather fetch cancelled.")
                }
                updateWeatherState { currentState -> currentState.copy(isLoadingWeather = false) }
                weatherWorkerUId = null
            }

            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                Log.d(TAG, "Work is ${workInfo.state}.")
                // Ensure loading is true if work is active
                if (!_uiState.value.weatherState.isLoadingWeather) { // Avoid unnecessary updates
                    updateWeatherState { currentState -> currentState.copy(isLoadingWeather = true) }
                    _uiState.update { it.copy(error = null) }
                }
            }
        }
    }

    // Helper function to map the API response (remains the same logic)
    private fun mapResponseToDisplayData(response: WeatherConditionResponse): WeatherDisplayData {
        val formattedTempFahrenheit = "${response.temperature?.imperial?.value ?: "--"}°${response.temperature?.imperial?.unit ?: ""}"
        val formattedTempCelsius = "${response.temperature?.metric?.value ?: "--"}°${response.temperature?.metric?.unit ?: ""}"
        val observedTime = response.localObservationDateTime?.substringAfter("T")?.substringBefore("+")?.substringBefore("-") ?: "N/A"

        return WeatherDisplayData(
            weatherDescription = response.weatherText ?: "No description",
            weatherIcon = WeatherIconMapper.getIconResource(response.weatherIcon ?: 0, response.isDayTime ?: true),
            temperatureFahrenheit = formattedTempFahrenheit,
            temperatureCelsius = formattedTempCelsius,
            observedAt = observedTime
        )
    }

    private fun updateLocationState(
        update: (currentState: LocationSelectionState) -> LocationSelectionState) {
        _uiState.update { currentState ->
            currentState.copy(locationState = update(currentState.locationState))
        }
    }

    private fun updateWeatherState(
        update: (currentState: WeatherDataState) -> WeatherDataState) {
        _uiState.update { currentState ->
            currentState.copy(weatherState = update(currentState.weatherState))
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }

}