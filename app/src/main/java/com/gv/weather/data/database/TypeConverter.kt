package com.gv.weather.data.database

import androidx.room.TypeConverter
import com.gv.weather.core.dateToString
import com.gv.weather.core.stringToDate
import com.gv.weather.data.database.details.CurrentWeather
import com.gv.weather.data.database.details.DayForecast
import com.gv.weather.data.database.details.Location
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class TypeConverter {
    private val json = Json { encodeDefaults = true }

    @TypeConverter
    fun List<DayForecast>.listDayForecastToString() = json.encodeToString(this)

    @TypeConverter
    fun String.toListDayForecast() = json.decodeFromString<List<DayForecast>>(this)

    @TypeConverter
    fun Location.locationToString() = "$country,$administrativeArea,$city"

    @TypeConverter
    fun String.toLocation() = split(",").run { Location(get(0), get(1), get(2)) }

    @TypeConverter
    fun CurrentWeather.currentWeatherToString() = "$temperature,$weather"

    @TypeConverter
    fun String.toCurrentWeather() =
        split(",").run { CurrentWeather(get(0).toDouble(), get(1)) }

    @TypeConverter
    fun Date?.nullableDateToString() = this?.dateToString() ?: ""

    @TypeConverter
    fun String.toDate(): Date? = if (this != "") stringToDate() else null
}