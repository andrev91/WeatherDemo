package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.adventure.api.ApiService
import com.example.adventure.data.WeatherLocationResponse
import com.example.adventure.network.NetworkModule
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SearchWorker @AssistedInject constructor(@Assisted context: Context, @Assisted params: WorkerParameters,
   private val apiService: ApiService,
   private val gson: Gson,
   @NetworkModule.ApiKey private val apiKey: String // Inject API key safely
    ) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val inputData = inputData.getString(SEARCH_KEY)?: return@withContext Result.failure(
                workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No input data provided")
            )
            // Search for location in the US
            val response = apiService.searchLocation("$inputData, US", apiKey)
            if (response.isSuccessful) {
                val body = response.body()
                if (!body.isNullOrEmpty()) {
                    val searchData: WeatherLocationResponse = body[0]
                    val resultJson = gson.toJson(searchData)

                    if (resultJson.toByteArray().size >= Data.MAX_DATA_BYTES) {
                        Log.e(TAG, "Serialized weather data exceeds WorkManager limit!")
                        Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Response data too large"))
                    }

                    Log.d(TAG, "Location search successful. JSON size: ${resultJson.toByteArray().size}")
                    val outputData = workDataOf(
                        OUTPUT_SUCCESS to true,
                        SEARCH_KEY to inputData,
                        LOCATION_JSON to resultJson
                    )
                    Result.success(outputData)
                } else {
                    Log.w(TAG, "Location search API success but body was null or empty.")
                    Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "Empty response from server"))
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
        const val LOCATION_JSON = "location_json"
        const val OUTPUT_SUCCESS = "SUCCESS" // Boolean
        const val OUTPUT_ERROR_MESSAGE = "ERROR_MSG" // Output for errors
        const val TAG = "WeatherFetchWorker"
    }

}