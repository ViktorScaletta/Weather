package com.gv.weather.data.api.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyForecastTemperature(
    @SerialName("Minimum") val minimum: TemperatureDetail,
    @SerialName("Maximum") val maximum: TemperatureDetail
)