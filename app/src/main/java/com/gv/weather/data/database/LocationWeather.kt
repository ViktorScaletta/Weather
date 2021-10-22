package com.gv.weather.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gv.weather.data.database.details.CurrentWeather
import com.gv.weather.data.database.details.DateSerializer
import com.gv.weather.data.database.details.DayForecast
import com.gv.weather.data.database.details.Location
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Entity(tableName = "Weather")
data class LocationWeather(
    @PrimaryKey var key: String = "",
    var location: Location = Location(),
    var currentWeather: CurrentWeather = CurrentWeather(),
    var dailyForecasts: List<DayForecast> = listOf(),
    var lastUpdateDate: @Serializable(DateSerializer::class) Date? = null
)
