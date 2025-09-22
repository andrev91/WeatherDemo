package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.adventure.api.ApiService
import com.example.adventure.data.network.model.GeocodingResponse
import com.example.adventure.network.NetworkModule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@HiltWorker
class SearchWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    @NetworkModule.ApiKey private val apiKey: String // Inject API key safely
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val searchQuery = inputData.getString(SEARCH_KEY) ?: return@withContext Result.failure(
                workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No input data provided")
            )

            val response = apiService.getLocation(searchQuery, 5, apiKey)
            if (response.isSuccessful) {
                val locations = response.body()
                if (!locations.isNullOrEmpty()) {
                    val location = locations[0] // Take the first result
                    val outputData = workDataOf(
                        OUTPUT_SUCCESS to true,
                        OUTPUT_LAT to location.lat,
                        OUTPUT_LON to location.lon
                    )
                    Result.success(outputData)
                } else {
                    Log.w(TAG, "Location search API success but body was null or empty.")
                    Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No location found for query: $searchQuery"))
                }
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(workDataOf(OUTPUT_ERROR_MESSAGE to errorMessage, OUTPUT_SUCCESS to false))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error searching location", e)
            Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Unexpected error: ${e.message}"))
        }
    }

    companion object {
        const val SEARCH_KEY = "search"
        const val OUTPUT_LAT = "output_lat"
        const val OUTPUT_LON = "output_lon"
        const val OUTPUT_SUCCESS = "SUCCESS" // Boolean
        const val OUTPUT_ERROR_MESSAGE = "ERROR_MSG" // Output for errors
        const val TAG = "SearchWorker"
    }
}