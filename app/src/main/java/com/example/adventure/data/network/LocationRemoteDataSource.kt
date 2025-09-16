package com.example.adventure.data.network

import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.adventure.worker.SearchWorker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class LocationRemoteDataSource @Inject constructor(
    private val workManager: WorkManager
) {

    fun fetchLocationCoordinates(searchQuery: String): Flow<Result<Pair<Double, Double>>> = callbackFlow {
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
                    val lat = workInfo.outputData.getDouble(SearchWorker.OUTPUT_LAT, Double.NaN)
                    val lon = workInfo.outputData.getDouble(SearchWorker.OUTPUT_LON, Double.NaN)

                    if (!lat.isNaN() && !lon.isNaN()) {
                        trySend(Result.success(Pair(lat, lon)))
                    } else {
                        trySend(Result.failure(Exception("Invalid coordinates from SearchWorker")))
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

        val liveData = workManager.getWorkInfoByIdLiveData(searchRequest.id)
        liveData.observeForever(workObserver)

        awaitClose {
            liveData.removeObserver(workObserver)
        }
    }
}