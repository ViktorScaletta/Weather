package com.gv.weather.data.database.details

import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeather(
    var temperature: Double = 0.0,
    var weather: String = ""
)