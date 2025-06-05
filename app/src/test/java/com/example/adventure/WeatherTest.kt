package com.example.adventure

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.work.Operation
import app.cash.turbine.test
import com.example.adventure.viewmodel.MainViewModel
import com.example.adventure.worker.USLocationWorker
import com.google.common.util.concurrent.Futures
import com.google.gson.Gson
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.util.UUID


@ExperimentalCoroutinesApi
class WeatherTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionID : UUID
    private lateinit var mockWorkInfo : MutableStateFlow<WorkInfo>

    @Mock
    private lateinit var mockWorkManager : WorkManager

    @Mock
    private lateinit var mockGson : Gson

    @Mock
    private lateinit var viewModel : MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        transactionID = UUID.randomUUID()
        val succeededWorkInfo = WorkInfo(
            transactionID,
            WorkInfo.State.ENQUEUED,
            emptySet(),
        )
        mockWorkInfo = MutableStateFlow(succeededWorkInfo)

        whenever(mockWorkManager.getWorkInfoByIdFlow(any())).thenReturn(mockWorkInfo)
        whenever(mockWorkManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>())).thenReturn(mock())

        viewModel = MainViewModel(mockWorkManager, mockGson)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `init state of view model and fetching locations`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse("isLoadingWeatherData be false initially", initialState.isLoadingWeatherData)
            assertFalse("isLoadingLocationData be false initially", initialState.isLoadingLocationData)
            assertTrue("isLoadingLocationList will be true on load", initialState.isLoadingLocationList)
            assertNull("weatherDisplayData should be null  initially", initialState.weatherDisplayData)
            assertNull("locationDisplayData should be null initially", initialState.locationDisplayData)
            assertNull("selectedLocation should be null initially", initialState.selectedLocation)
            assertNull("error should be null initially", initialState.error)
            assertTrue("locationList should be empty initially", initialState.availableLocations.isNullOrEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `init view model finished loading location list`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            val succeededWorkInfo = WorkInfo(
                transactionID,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                workDataOf(USLocationWorker.LOCATION_JSON to Gson().toJson(listOf("Atlanta", "Tucker", "Sandy Springs")), USLocationWorker.OUTPUT_SUCCESS to true)
            )
            mockWorkInfo.value = succeededWorkInfo // Update the MutableStateFlow's value
            // THEN: The ViewModel's exposed states should now reflect SUCCEEDED
            val successState = awaitItem()
            assertFalse("Work should be fully loaded", successState.isLoadingLocationList)
            assertTrue("Work should not be running after succeeding", successState.availableLocations!!.isNotEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

}