package com.example.adventure.repository

import com.example.adventure.data.local.LocationDao
import com.example.adventure.data.local.model.Location

class LocationRepository(private val locationDao: LocationDao) {

    fun getLocationByKey(key: String) = locationDao.getLocationByKey(key)
    fun getLocationBySearchString(searchString: String) = locationDao.getLocationBySearchString(searchString)

    suspend fun insertLocation(location: Location) = locationDao.insertLocation(location)
    suspend fun deleteLocation(location: Location) = locationDao.deleteLocation(location)

}