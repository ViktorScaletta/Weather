package com.gv.weather.domain

import com.gv.weather.data.LocationSearchItem
import com.gv.weather.data.database.LocationWeather
import kotlinx.coroutines.flow.Flow

interface Interactor {

    fun locationsWeatherFlow(): Flow<List<LocationWeather>>

    fun locationWeatherFlow(key: String): Flow<LocationWeather?>

    suspend fun updateWeather(key: String, lang: String)

    suspend fun obtainLocations(query: String, lang: String): List<LocationSearchItem>

    suspend fun addLocation(locationSearchItem: LocationSearchItem)

    suspend fun deleteLocation(position: Int)

}