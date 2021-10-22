package com.gv.weather.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MainDao {

    @Query("SELECT * FROM Weather")
    fun locationsWeatherFlow(): Flow<List<LocationWeather>>

    @Query("SELECT * FROM Weather")
    fun locationsWeather(): List<LocationWeather>

    @Query("SELECT * FROM Weather WHERE `key` = :key")
    fun locationWeatherFlow(key: String): Flow<LocationWeather?>

    @Query("SELECT * FROM Weather WHERE `key` = :key")
    fun locationWeather(key: String): LocationWeather?

    @Query("DELETE FROM Weather")
    suspend fun clearWeatherData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationWeather: LocationWeather): Long

    @Update
    suspend fun update(locationWeather: LocationWeather)

    @Delete
    suspend fun delete(locationWeather: LocationWeather)

}