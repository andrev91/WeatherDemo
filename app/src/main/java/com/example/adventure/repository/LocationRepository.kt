package com.example.adventure.repository

import com.example.adventure.data.local.LocationDao
import com.example.adventure.data.local.model.Location
import com.example.adventure.data.network.LocationRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val locationRemoteDataSource: LocationRemoteDataSource) {

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

}