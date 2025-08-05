package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
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
class LocationKeyWorker @AssistedInject constructor(@Assisted context: Context, @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    @NetworkModule.ApiKey private val apiKey: String // Inject API key safely
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val inputData = inputData.getString(LOCATION_KEY)?: return@withContext Result.failure(
            workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No input data provided")
        )

        try {
            Log.d(TAG, "Fetching location data for key: $inputData")
            val request = apiService.getLocation(inputData, apiKey)
            if (request.isSuccessful) {
                val body = request.body()
                if (body != null) {
                    val outputData = workDataOf(
                        OUTPUT_SUCCESS to true,
                        LOCATION_KEY to inputData,
                        LOCATION_JSON to Json.encodeToString(body)
                    )
                    Result.success(outputData)
                } else {
                    Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Empty response from server"))
                }
            } else {
                Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Error fetching location"))
            }
        } catch (e : Exception) {
            Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to e.message))
        }

    }

    companion object {
        const val LOCATION_KEY = "location"
        const val LOCATION_JSON = "locationJson"
        const val OUTPUT_SUCCESS = "SUCCESS" // Boolean
        const val OUTPUT_ERROR_MESSAGE = "ERROR_MSG" // Output for errors
        const val TAG = "LocationFetchWorker"

    }
}