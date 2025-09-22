package com.example.adventure.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.adventure.data.model.USState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

@HiltWorker
class USLocationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    // The OpenWeatherMap API does not provide an endpoint to fetch all US locations.
    // Therefore, this worker reads the data from a local `us_state.json` asset file.
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching location data for US States from assets")
            val jsonString = context.assets.open("us_state.json").bufferedReader().use { it.readText() }
            val states = Json.decodeFromString(ListSerializer(USState.serializer()), jsonString)

            if (states.isNotEmpty()) {
                val locationsJson = Json.encodeToString(ListSerializer(USState.serializer()), states)
                val outputData = workDataOf(
                    OUTPUT_SUCCESS to true,
                    LOCATION_JSON to locationsJson
                )
                Result.success(outputData)
            } else {
                Result.failure(workDataOf(OUTPUT_SUCCESS to false, OUTPUT_ERROR_MESSAGE to "No locations found in asset file."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading or parsing us_state.json", e)
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