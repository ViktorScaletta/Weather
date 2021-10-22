package com.gv.weather.data.database.details

import kotlinx.serialization.Serializable

@Serializable
data class DayForecast(
    var date: String = "",
    var dayWeather: Pair<Double, String> = Pair(0.0, ""),
    var nightWeather: Pair<Double, String> = Pair(0.0, "")
)