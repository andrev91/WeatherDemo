package com.example.adventure.repository

import android.content.Context
import android.util.Log
import com.example.adventure.data.local.LocationDao
import com.example.adventure.data.local.model.Location
import com.example.adventure.data.network.LocationRemoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationDao,
    private val locationRemoteDataSource: LocationRemoteDataSource) {

    @kotlinx.serialization.Serializable
    data class StateCities(
        @SerialName("major_cities") val majorCities: List<String> = emptyList(),
        @SerialName("all_cities")val allCities: List<String> = emptyList()
    )
    @kotlinx.serialization.Serializable
    data class State(
        val name: String,
        @SerialName("code") val abbreviation: String
    )

    fun getLocationByKey(key: String) = locationDao.getLocationByKey(key)
    fun getOrFetchLocation(name: String) : Flow<Result<Location>> = flow {
        val cachedLocationKey = locationDao.getLocationBySearchString(name).firstOrNull()
        if (cachedLocationKey != null) {
            emit(Result.success(cachedLocationKey))
            return@flow
        }

        locationRemoteDataSource.fetchLocationKey(name).collect { result ->
            result.onSuccess { key ->
                val location = Location(name = name, locationKey = key)
                locationDao.insertLocation(location)
                emit(Result.success(location))
            }.onFailure { error ->
                emit(Result.failure(error))
            }
        }
    }

    private val _citiesData: Map<String, StateCities> by lazy {
        try {
            val json = context.assets.open("us_state_cities.json").bufferedReader().use { it.readText() }
            val parsedData : Map<String, StateCities> = Json.decodeFromString(json)
            return@lazy parsedData
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error loading cities data", e)
            return@lazy emptyMap()
        }
    }

    private val _statesData: List<State> by lazy {
        try {
            val json = context.assets.open("us_state.json").bufferedReader().use { it.readText() }
            val parsedData : List<State> = Json.decodeFromString(json)
            return@lazy parsedData
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error loading states data", e)
            return@lazy emptyList()
        }
    }

    fun getStates(): List<State> {
        return _statesData
    }

    fun getCities(): Map<String, StateCities> {
        return _citiesData
    }

    /**
     * Returns a list of ALL cities in the given state.
     * Expected param for state is the abbreviation. IE "CA" for California
     */
    fun getCitiesByState(state: String): List<String> {
        return _citiesData[state]?.allCities ?: emptyList()
    }

    /**
     * Returns a list of MAJOR cities in the given state.
     * Expected param for state is the abbreviation. IE "CA" for California
     */
    fun getMajorCitiesByState(state: String): List<String> {
        return _citiesData[state]?.majorCities ?: emptyList()
    }


}