package com.example.adventure.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adventure.data.local.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Query("SELECT * FROM location WHERE locationKey = :key")
    fun getLocationByKey(key: String) : Flow<Location?>

    @Query("SELECT * FROM location WHERE name = :searchString")
    fun getLocationBySearchString(searchString: String) : Flow<Location?>
    @Delete
    fun deleteLocation(location: Location)

}