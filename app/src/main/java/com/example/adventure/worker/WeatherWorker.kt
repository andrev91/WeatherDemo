package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.adventure.api.ApiService
import com.example.adventure.network.NetworkModule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@HiltWorker
class WeatherWorker @AssistedInject constructor(@Assisted context: Context, @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    @NetworkModule.ApiKey private val apiKey: String // Inject API key safely
    ) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val lat = inputData.getDouble(WEATHER_LAT_KEY, Double.NaN)
            val lon = inputData.getDouble(WEATHER_LON_KEY, Double.NaN)

            if (lat.isNaN() || lon.isNaN()) {
                return@withContext Result.failure(
                    workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Invalid lat/lon provided")
                )
            }

            val response = apiService.getWeather(lat, lon, apiKey)
            if (response.isSuccessful) {
                val weatherData = response.body()
                if (weatherData != null) {
                    val weatherJson = Json.encodeToString(weatherData)

                    if (weatherJson.toByteArray().size >= Data.MAX_DATA_BYTES) {
                        Log.e(TAG, "Serialized weather data exceeds WorkManager limit!")
                        Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Response data too large"))
                    } else {
                        Log.d(TAG, "Weather fetch successful. JSON size: ${weatherJson.toByteArray().size}")
                        val outputData = workDataOf(
                            OUTPUT_SUCCESS to true,
                            WEATHER_JSON to weatherJson
                        )
                        Result.success(outputData)
                    }
                } else {
                    Log.w(TAG, "Weather fetch API success but body was null.")
                    Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Empty response from server"))
                }
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(workDataOf(OUTPUT_ERROR_MESSAGE to errorMessage, OUTPUT_SUCCESS to false))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching weather", e)
            Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Unexpected error: ${e.message}"))
        }
    }

    companion object {
        const val WEATHER_LAT_KEY = "weather_lat"
        const val WEATHER_LON_KEY = "weather_lon"
        const val WEATHER_JSON = "weather_json"
        const val OUTPUT_SUCCESS = "SUCCESS" // Boolean
        const val OUTPUT_ERROR_MESSAGE = "ERROR_MSG" // Output for errors
        const val TAG = "WeatherFetchWorker"
    }

}