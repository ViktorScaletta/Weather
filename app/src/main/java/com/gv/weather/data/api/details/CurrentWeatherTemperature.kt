package com.gv.weather.data.api.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherTemperature(
    @SerialName("Metric") val metric: TemperatureDetail,
    @SerialName("Imperial") val imperial: TemperatureDetail
)