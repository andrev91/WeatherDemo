package com.example.adventure.data.network

import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.adventure.data.network.model.WeatherLocationResponse
import com.example.adventure.worker.SearchWorker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class LocationRemoteDataSource @Inject constructor(
    private val workManager: WorkManager) {

    fun fetchLocationKey(searchQuery: String): Flow<Result<String>> = callbackFlow {
        val searchRequest = OneTimeWorkRequestBuilder<SearchWorker>()
            .setInputData(workDataOf(SearchWorker.SEARCH_KEY to searchQuery))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork(
            "SearchWorker: $searchQuery",
            ExistingWorkPolicy.REPLACE,
            searchRequest
        )

        val workObserver = Observer<WorkInfo> { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val locationJson = workInfo.outputData.getString(SearchWorker.LOCATION_JSON)
                    val locationData = Json.decodeFromString<WeatherLocationResponse>(locationJson!!)
                    val key = locationData.key
                    if (key != null) {
                        trySend(Result.success(key)) // Send success result
                    } else {
                        trySend(Result.failure(Exception("Location key was null in response.")))
                    }
                    channel.close()
                }

                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString(SearchWorker.OUTPUT_ERROR_MESSAGE)
                    trySend(Result.failure(Exception(error ?: "WorkManager failed")))
                    channel.close()
                }

                WorkInfo.State.CANCELLED -> {
                    trySend(Result.failure(CancellationException("Search was cancelled")))
                    channel.close()
                }

                else -> { /* Blocked, Enqueued, Running */ }
            }
        }

        //Convert LiveData to Flow by observing it
        val liveData = workManager.getWorkInfoByIdLiveData(searchRequest.id)
        liveData.observeForever(workObserver)

        //Crucial for cleaning up the observer when the flow is cancelled
        awaitClose {
            liveData.removeObserver(workObserver)
        }
    }

}