package com.example.adventure.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
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
import com.example.adventure.worker.LocationKeyWorker
import com.example.adventure.worker.WeatherWorker
import com.google.gson.Gson
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
    val temperature: String,
    val weatherDescription: String,
    val observedAt : String,
)

data class LocationDisplayData(
    val locationName: String,
    val adminArea: String,
    val country : String
)

data class WeatherUiState(
    val isLoadingWeatherData: Boolean = false,
    val isLoadingLocationData: Boolean = false,
    val weatherDisplayData: WeatherDisplayData? = null,
    val locationDisplayData: LocationDisplayData? = null,
    val error: String? = null
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

    private val locationKey = "225007"

    init {
      fetchData()
    }

    fun fetchData() {
        if (_uiState.value.isLoadingWeatherData || _uiState.value.isLoadingLocationData) return

        _uiState.update { it.copy(isLoadingWeatherData = true ,
            isLoadingLocationData = true, error = null, weatherDisplayData = null) } //Initial Load

        var oneTimeWorkRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInputData(
                workDataOf(WeatherWorker.WEATHER_KEY to locationKey)
            ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        weatherWorkerUId = oneTimeWorkRequest.id
        observerWork(weatherWorkerUId!!)
        workManager.enqueueUniqueWork(
            "WeatherLocation_$locationKey",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest)

        oneTimeWorkRequest = OneTimeWorkRequestBuilder<LocationKeyWorker>()
            .setInputData(workDataOf(LocationKeyWorker.LOCATION_KEY to locationKey))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        locationWorkerUID = oneTimeWorkRequest.id
        observerWork(locationWorkerUID!!)
        workManager.enqueueUniqueWork(
            "Location_$locationKey",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest)
    }

    private fun observerWork(uuid: UUID) {
        workManager.getWorkInfoByIdFlow(uuid)
            .filterNotNull()
            .filter { it.id == weatherWorkerUId || it.id == locationWorkerUID }
            .onEach { workInfo -> process(workInfo) }
            .launchIn(viewModelScope)
    }

    private fun process(workInfo: WorkInfo) {
        Log.d("WeatherViewModel", "Processing WorkInfo: ID=${workInfo.id}, State=${workInfo.state}")
        if (workInfo.id == locationWorkerUID) {
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
                    Log.e("WeatherViewModel", "Work failed: $errorMsg")
                }
                WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                    Log.d("WeatherViewModel", "Work is ${workInfo.state}.")
                    // Ensure loading is true if work is active
                    if (!_uiState.value.isLoadingLocationData) { // Avoid unnecessary updates
                        _uiState.update { it.copy(isLoadingLocationData = true, error = null) }
                    }
                }
            }
        } else if (workInfo.id == weatherWorkerUId) {
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
                                Log.d(
                                    "WeatherViewModel",
                                    "Successfully parsed weather data from worker."
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "WeatherViewModel",
                                    "Error parsing JSON from worker output",
                                    e
                                )
                                _uiState.update {
                                    it.copy(
                                        isLoadingWeatherData = false,
                                        error = "Failed to parse weather data."
                                    )
                                }
                            }
                        } else {
                            Log.e("WeatherViewModel", "Work succeeded but weather JSON was null.")
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
                            "WeatherViewModel",
                            "Work succeeded but internal flag was false: $errorMsg"
                        )
                        _uiState.update { it.copy(isLoadingWeatherData = false, error = errorMsg) }
                    }
                    weatherWorkerUId = null // Reset tracked ID once finished
                }

                WorkInfo.State.FAILED -> {
                    val errorMsg = workInfo.outputData.getString(WeatherWorker.OUTPUT_ERROR_MESSAGE)
                        ?: "Unknown error"
                    Log.e("WeatherViewModel", "Work failed: $errorMsg")
                    _uiState.update { it.copy(isLoadingWeatherData = false, error = errorMsg) }
                    weatherWorkerUId = null
                }

                WorkInfo.State.CANCELLED -> {
                    Log.w("WeatherViewModel", "Work cancelled.")
                    _uiState.update {
                        it.copy(
                            isLoadingWeatherData = false,
                            error = "Weather fetch cancelled."
                        )
                    }
                    weatherWorkerUId = null
                }

                WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                    Log.d("WeatherViewModel", "Work is ${workInfo.state}.")
                    // Ensure loading is true if work is active
                    if (!_uiState.value.isLoadingWeatherData) { // Avoid unnecessary updates
                        _uiState.update { it.copy(isLoadingWeatherData = true, error = null) }
                    }
                }
            }
        }
    }

    // Helper function to map the API response (remains the same logic)
    private fun mapResponseToDisplayData(response: WeatherConditionResponse): WeatherDisplayData {
        val tempValue = response.temperature?.metric?.value ?: response.temperature?.imperial?.value ?: "--"
        val tempUnit = response.temperature?.metric?.unit ?: response.temperature?.imperial?.unit ?: ""
        val formattedTemp = "$tempValue°$tempUnit"
        val observedTime = response.localObservationDateTime?.substringAfter("T")?.substringBefore("+")?.substringBefore("-") ?: "N/A"

        return WeatherDisplayData(
            weatherDescription = response.weatherText ?: "No description",
            temperature = formattedTemp,
            observedAt = observedTime
        )
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