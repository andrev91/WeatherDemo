package com.example.adventure.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.adventure.data.WeatherConditionResponse
import com.example.adventure.data.WeatherLocationResponse
import com.example.adventure.state.WeatherUiState
import com.example.adventure.util.WeatherIconMapper
import com.example.adventure.worker.LocationKeyWorker
import com.example.adventure.worker.SearchWorker
import com.example.adventure.worker.USLocationWorker
import com.example.adventure.worker.WeatherWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

data class LocationDisplayData(
    val locationName: String,
    val adminArea: String,
    val country : String
)

data class LocationOption(
    val key: String,
    val value: String
)

//sealed class testChannel {
//    data class Error(val errorMessage: String) : testChannel()
//    data class Success(val successMessage: String) : testChannel()
//}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val gson: Gson,
    ) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

//    private val _testChannel = Channel<testChannel>()
//    val flowChannel: Flow<testChannel> = _testChannel.receiveAsFlow()

    private var weatherWorkerUId: UUID? = null
    private var locationWorkerUID: UUID? = null
    private var locationListWorkerUID: UUID? = null

    private val locationKey = "225007" //default location

    init {
        fetchLocationList()
    }

    fun setSelectedLocation(location: LocationOption) {
        if (location == _uiState.value.selectedLocation) return

        _uiState.update { it.copy(selectedLocation = location, weatherDisplayData = null, locationDisplayData = null, error = null) }
    }

    private fun fetchWeatherAndLocation(locationKey: String = "") {
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

        val locationRequest = OneTimeWorkRequestBuilder<LocationKeyWorker>()
            .setInputData(workDataOf(LocationKeyWorker.LOCATION_KEY to locationKey.ifEmpty { this.locationKey }))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        locationWorkerUID = locationRequest.id
        observerLocationWork(locationWorkerUID!!)
        workManager.enqueueUniqueWork(
            "Location_$locationKey",
            ExistingWorkPolicy.REPLACE,
            locationRequest)
    }

    fun searchLocation() {
        if (_uiState.value.isLoadingLocationData || _uiState.value.isLoadingWeatherData) return
        _uiState.update { it.copy(isLoadingWeatherData = true ,
            isLoadingLocationData = true, error = null, weatherDisplayData = null, locationDisplayData = null) }

        val location = uiState.value.selectedLocation?.value ?: return
        val searchRequest = OneTimeWorkRequestBuilder<SearchWorker>()
            .setInputData(workDataOf(SearchWorker.SEARCH_KEY to location))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        val uuid = searchRequest.id
        workManager.enqueueUniqueWork(
            "SearchWorker: $location",
            ExistingWorkPolicy.REPLACE,
            searchRequest)
        workManager.getWorkInfoByIdLiveData(uuid)
            .asFlow()
            .onEach { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val outputData = workInfo.outputData
                    val locationData = gson.fromJson(outputData.getString(SearchWorker.LOCATION_JSON), WeatherLocationResponse::class.java)
                    if (locationData != null) {
                        fetchWeatherAndLocation(locationData.key!!)
                    }
                } else if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
                    val errorMessage = workInfo.outputData.getString(SearchWorker.OUTPUT_ERROR_MESSAGE)
                    _uiState.update { it.copy(error = errorMessage) }
                }
            }.launchIn(viewModelScope)
    }

    fun triggerTempTypeChange(UName: UnitType) {
        _uiState.update { it.copy(temperatureUnit = UName) }
    }

    private fun fetchLocationList() {
        if (_uiState.value.isLoadingLocationList) return

        _uiState.update { it.copy(isLoadingLocationList = true,
            error = null, selectedLocation = null) }

        val locationListRequest = OneTimeWorkRequestBuilder<USLocationWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        locationListWorkerUID = locationListRequest.id
        observeLocationListWork(locationListWorkerUID!!)
        workManager.enqueueUniqueWork(
            "Location_loader",
            ExistingWorkPolicy.REPLACE,
            locationListRequest)
    }

    private fun observerWeatherWork(uuid: UUID) {
        workManager.getWorkInfoByIdFlow(uuid)
            .filterNotNull()
            .filter { it.id == weatherWorkerUId }
            .onEach { workInfo -> processWeather(workInfo) }
            .launchIn(viewModelScope)
    }

    private fun observerLocationWork(uuid: UUID) {
        workManager.getWorkInfoByIdFlow(uuid)
            .filterNotNull()
            .filter { it.id == locationWorkerUID }
            .onEach { workInfo -> processLocation(workInfo) }
            .launchIn(viewModelScope)
    }

    private fun observeLocationListWork(uuid: UUID) {
        workManager.getWorkInfoByIdFlow(uuid)
            .filterNotNull()
            .filter { it.id == locationListWorkerUID }
            .onEach { workInfo -> processLocationList(workInfo) }
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
                            val response =
                                gson.fromJson(weatherJson, WeatherConditionResponse::class.java)
                            _uiState.update {
                                it.copy(
                                    isLoadingWeatherData = false,
                                    weatherDisplayData = mapResponseToDisplayData(response), // Use helper
                                    error = null
                                )
                            }
                            Log.d(TAG,"Successfully parsed weather data from worker.")
                        } catch (e: Exception) {
                            Log.e(TAG,"Error parsing JSON from worker output",e)
                            _uiState.update {
                                it.copy(
                                    isLoadingWeatherData = false,
                                    error = "Failed to parse weather data."
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "Work succeeded but weather JSON was null.")
                        _uiState.update {
                            it.copy(
                                isLoadingWeatherData = false,
                                error = "Received empty success response."
                            )
                        }
                    }
                } else {
                    // Worker finished but reported internal failure
                    val errorMsg = outputData.getString(WeatherWorker.OUTPUT_ERROR_MESSAGE)
                        ?: "Worker reported failure."
                    Log.e(
                        TAG,
                        "Work succeeded but internal flag was false: $errorMsg"
                    )
                    _uiState.update { it.copy(isLoadingWeatherData = false, error = errorMsg) }
                }
                weatherWorkerUId = null // Reset tracked ID once finished
            }

            WorkInfo.State.FAILED -> {
                val errorMsg = workInfo.outputData.getString(WeatherWorker.OUTPUT_ERROR_MESSAGE)
                    ?: "Unknown error"
                Log.e(TAG, "Work failed: $errorMsg")
                _uiState.update { it.copy(isLoadingWeatherData = false, error = errorMsg) }
                weatherWorkerUId = null
            }

            WorkInfo.State.CANCELLED -> {
                Log.w(TAG, "Work cancelled.")
                _uiState.update {
                    it.copy(
                        isLoadingWeatherData = false,
                        error = "Weather fetch cancelled."
                    )
                }
                weatherWorkerUId = null
            }

            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                Log.d(TAG, "Work is ${workInfo.state}.")
                // Ensure loading is true if work is active
                if (!_uiState.value.isLoadingWeatherData) { // Avoid unnecessary updates
                    _uiState.update { it.copy(isLoadingWeatherData = true, error = null) }
                }
            }
        }
    }

    private fun processLocation(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                val outputData = workInfo.outputData
                val success = outputData.getBoolean(LocationKeyWorker.OUTPUT_SUCCESS, false)
                if (success) {
                    val locationJson = outputData.getString(LocationKeyWorker.LOCATION_JSON)
                    val gson = gson.fromJson(locationJson, WeatherLocationResponse::class.java)
                    _uiState.update { it.copy(isLoadingLocationData = false, error = null, locationDisplayData = getLocationDisplayData(gson)) }
                }
            }
            WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                val errorMsg = workInfo.outputData.getString(LocationKeyWorker.OUTPUT_ERROR_MESSAGE)
                    ?: "Unknown error"
                Log.e(TAG, "Work failed: $errorMsg")
            }
            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                Log.d(TAG, "Work is ${workInfo.state}.")
                // Ensure loading is true if work is active
                if (!_uiState.value.isLoadingLocationData) { // Avoid unnecessary updates
                    _uiState.update { it.copy(isLoadingLocationData = true, error = null) }
                }
            }
        }
    }

    private fun processLocationList(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                val outputData = workInfo.outputData
                val success = outputData.getBoolean(USLocationWorker.OUTPUT_SUCCESS, false)
                if (success) {
                    val locationJson = outputData.getString(USLocationWorker.LOCATION_JSON)
                    val type = object : TypeToken<List<WeatherLocationResponse>>() {}.type
                    val data = gson.fromJson<List<WeatherLocationResponse>>(locationJson, type)
                    _uiState.update { it.copy(availableLocations = data.map { location -> getLocationOptionData(location) },
                        isLoadingLocationList = false, error = null) }
                }
            }
            WorkInfo.State.FAILED , WorkInfo.State.CANCELLED -> {
                val errorMsg = workInfo.outputData.getString(USLocationWorker.OUTPUT_ERROR_MESSAGE)
                    ?: "Unknown error"
                Log.e(TAG, "Work failed: $errorMsg")
                _uiState.update { it.copy(availableLocations = emptyList(),
                    isLoadingLocationList = false, error = errorMsg) }
            }
            else -> {
                Log.d(TAG, "Work is ${workInfo.state}.")
                // Ensure loading is true if work is active
                if (!_uiState.value.isLoadingLocationList) { // Avoid unnecessary updates
                    _uiState.update { it.copy(isLoadingLocationList = true, error = null) }
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
            weatherIcon = WeatherIconMapper.getIconResource(response.weatherIcon ?: 0),
            temperatureFahrenheit = formattedTempFahrenheit,
            temperatureCelsius = formattedTempCelsius,
            observedAt = observedTime
        )
    }

    private fun getLocationOptionData(response: WeatherLocationResponse) :LocationOption {
        return LocationOption(
            value = response.localizedName ?: response.englishName ?: "Unknown City",
            key = response.key ?: "Unknown Key")
    }

    private fun getLocationDisplayData(response: WeatherLocationResponse) :LocationDisplayData {
        return LocationDisplayData(
            locationName = response.localizedName ?: response.englishName ?: "Unknown City",
            adminArea = response.administrativeArea?.localizedName ?: response.administrativeArea?.id ?: "Unknown Area",
            country = response.country?.localizedName ?: response.country?.id ?: "Unknown Country")
    }

    companion object {
        const val TAG = "MainViewModel"
    }

}