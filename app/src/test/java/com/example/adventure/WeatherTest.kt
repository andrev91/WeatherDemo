package com.example.adventure

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.cash.turbine.test
import com.example.adventure.data.network.model.WeatherLocationResponse
import com.example.adventure.repository.LocationRepository
import com.example.adventure.viewmodel.MainViewModel
import com.example.adventure.worker.USLocationWorker.Companion.LOCATION_JSON
import com.example.adventure.worker.USLocationWorker.Companion.OUTPUT_SUCCESS
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
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.util.UUID


@ExperimentalCoroutinesApi
class WeatherTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionID : UUID
    private lateinit var generatedWorkerUID : UUID
    private lateinit var mockWorkInfo : MutableStateFlow<WorkInfo>

    @Mock
    private lateinit var mockWorkManager : WorkManager

    @Mock
    private lateinit var mockLocationRepository : LocationRepository

    @Mock
    private lateinit var viewModel : MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        val workRequestCaptor = argumentCaptor<OneTimeWorkRequest>()

        whenever(mockWorkManager.enqueueUniqueWork(
            any<String>(),
            any<ExistingWorkPolicy>(),
            workRequestCaptor.capture()
        )).thenReturn(Mockito.mock(Operation::class.java))

        transactionID = UUID.randomUUID()
        val succeededWorkInfo = WorkInfo(
            transactionID,
            WorkInfo.State.ENQUEUED,
            emptySet(),
        )
        mockWorkInfo = MutableStateFlow(succeededWorkInfo)

        whenever(mockWorkManager.getWorkInfoByIdFlow(any())).thenReturn(mockWorkInfo)
//        whenever(mockWorkManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>())).thenReturn(mock())

        viewModel = MainViewModel(mockWorkManager, mockLocationRepository)
        generatedWorkerUID = workRequestCaptor.firstValue.id
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


            assertFalse("isLoadingWeatherData be false initially", initialState.weatherState.isLoadingWeather)
            assertFalse("isLoadingCityData be false initially", initialState.locationState.isLoadingCities)
            assertTrue("isLoadingStateList will be true on load", initialState.locationState.isLoadingStates)
            assertNull("weatherDisplayData should be null initially", initialState.weatherState.displayData)
            assertNull("selectedState should be null initially", initialState.locationState.selectedState)
            assertNull("selectedCity should be null initially", initialState.locationState.selectedCity)
            assertNull("error should be null initially", initialState.error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `init view model finished loading location list`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            val data1 = WeatherLocationResponse(key = null, englishName = "New York", localizedName = "New York",
                region = null, administrativeArea = null, country = null)
            val test = Json.encodeToString(data1)
            val succeededWorkInfo = WorkInfo(
                generatedWorkerUID,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                outputData = workDataOf(LOCATION_JSON to test, OUTPUT_SUCCESS to true)
            ) //Data {locationsJson : [{"LocalizedName":"New York","EnglishName":"New York"}], SUCCESS : true}
            mockWorkInfo.value = succeededWorkInfo // Update the MutableStateFlow's value
            // THEN: The ViewModel's exposed states should now reflect SUCCEEDED
            val successState = awaitItem()
            assertFalse("Work should be fully loaded", successState.locationState.isLoadingStates)
             assertTrue("Work should not be running after succeeding", successState.locationState.availableStates!!.isNotEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

}