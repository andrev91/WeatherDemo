package com.example.adventure.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.adventure.data.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val UNIT_KEY = stringPreferencesKey("temperature_unit")

    val temperatureUnit: Flow<TemperatureUnit> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val unitString = preferences[UNIT_KEY]
            if (unitString != null) {
                try {
                    TemperatureUnit.valueOf(unitString)
                } catch (e: IllegalArgumentException) {
                    TemperatureUnit.CELSIUS
                }
            } else {
                TemperatureUnit.CELSIUS
            }
        }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { preferences ->
            preferences[UNIT_KEY] = unit.name
        }
    }
}
