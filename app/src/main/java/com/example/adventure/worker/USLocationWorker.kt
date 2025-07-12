package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.adventure.api.ApiService
import com.example.adventure.data.network.model.WeatherLocationResponse
import com.example.adventure.network.NetworkModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class USLocationWorker @AssistedInject constructor(@Assisted context: Context, @Assisted params: WorkerParameters,
   private val apiService: ApiService,
   private val gson: Gson,
   @NetworkModule.ApiKey private val apiKey: String // Inject API key safely
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching location data for US States")
            val request = apiService.getUnitedStatesLocations(apiKey = apiKey)
            if (request.isSuccessful) {
                val body = request.body()
                if (body != null) {
                    if (body.isEmpty()) {
                        Log.w(TAG, "Fetched locations, but not able to return response.")
                        Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No displayable locations found."))
                    }

                    val listType = object : TypeToken<List<WeatherLocationResponse>>() {}.type
                    val locationsJson = gson.toJson(body, listType)

                    val outputData = workDataOf(
                        OUTPUT_SUCCESS to true,
                        LOCATION_JSON to locationsJson
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
        const val LOCATION_JSON = "locationsJson"
        const val OUTPUT_SUCCESS = "SUCCESS" // Boolean
        const val OUTPUT_ERROR_MESSAGE = "ERROR_MSG" // Output for errors
        const val TAG = "USLocationWorker"
    }
}