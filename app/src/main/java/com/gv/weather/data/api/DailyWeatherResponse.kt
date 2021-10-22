package com.gv.weather.data.api

import com.gv.weather.data.api.details.DailyForecast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DailyWeatherResponse(
    @SerialName("Headline") val headline: JsonObject,
    @SerialName("DailyForecasts") val dailyForecasts: List<DailyForecast>
)