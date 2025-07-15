package com.example.adventure.repository

import android.content.Context
import com.example.adventure.data.local.LocationDao
import com.example.adventure.data.local.model.Location
import com.example.adventure.data.network.LocationRemoteDataSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val locationDao: LocationDao,
    private val locationRemoteDataSource: LocationRemoteDataSource) {

    data class StateCities(
        val majorCities: List<String> = emptyList(),
        val allCities: List<String> = emptyList()
    )
    data class State(
        val name: String,
        val abbreviation: String
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

    private val cities: Map<String, StateCities> by lazy {
        val json = context.assets.open("us_state_cities.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, object : TypeToken<Map<String, StateCities>>() {}.type)
    }

    private val states: List<State> by lazy {
        val json = context.assets.open("us_states.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, object : TypeToken<List<State>>() {}.type)

    }

    fun getStates(): List<State> {
        return states
    }

    fun getCities(): Map<String, StateCities> {
        return cities
    }

    /**
     * Returns a list of ALL cities in the given state.
     * Expected param for state is the abbreviation. IE "CA" for California
     */
    fun getCitiesByState(state: String): List<String> {
        return cities[state]?.allCities ?: emptyList()
    }

    /**
     * Returns a list of MAJOR cities in the given state.
     * Expected param for state is the abbreviation. IE "CA" for California
     */
    fun getMajorCitiesByState(state: String): List<String> {
        return cities[state]?.majorCities ?: emptyList()
    }


}