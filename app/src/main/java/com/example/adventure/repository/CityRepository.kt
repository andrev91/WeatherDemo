package com.example.adventure.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    data class City(
        val majorCities: List<String> = emptyList(),
        val allCities: List<String> = emptyList()
    )

    private val locations: Map<String, City> by lazy {
        val json = context.assets.open("us_state_cities.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, object : TypeToken<Map<String, City>>() {}.type)
    }

    fun getCitiesByState(state: String): List<String> {
        return locations[state]?.allCities ?: emptyList()
    }

    fun getMajorCitiesByState(state: String): List<String> {
        return locations[state]?.majorCities ?: emptyList()
    }

}