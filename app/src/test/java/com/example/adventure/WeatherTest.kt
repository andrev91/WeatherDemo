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
import com.example.adventure.data.repository.LocationRepository
import com.example.adventure.viewmodel.WeatherViewModel
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
    private lateinit var viewModel : WeatherViewModel

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

        viewModel = WeatherViewModel(mockWorkManager, mockLocationRepository)
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


    @Test
    fun `searchLocation success fetchesWeather`() = runTest {
        val mockLocation = com.example.adventure.data.local.model.Location(name = "New York", latitude = 40.7128, longitude = -74.0060)
        whenever(mockLocationRepository.getOrFetchLocation(any())).thenReturn(MutableStateFlow(Result.success(mockLocation)))

        viewModel.searchLocation()

        val weatherRequestCaptor = argumentCaptor<OneTimeWorkRequest>()
        Mockito.verify(mockWorkManager).enqueueUniqueWork(
            any<String>(),
            any<ExistingWorkPolicy>(),
            weatherRequestCaptor.capture()
        )

        val weatherWorkRequest = weatherRequestCaptor.firstValue
        assertTrue(weatherWorkRequest.workSpec.input.getDouble(com.example.adventure.worker.WeatherWorker.WEATHER_LAT_KEY, 0.0) == 40.7128)
        assertTrue(weatherWorkRequest.workSpec.input.getDouble(com.example.adventure.worker.WeatherWorker.WEATHER_LON_KEY, 0.0) == -74.0060)
    }
}