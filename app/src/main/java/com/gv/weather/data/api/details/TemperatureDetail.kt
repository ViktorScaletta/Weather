package com.gv.weather.data.api.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemperatureDetail(
    @SerialName("Value") val value: Double,
    @SerialName("Unit") val unit: String,
    @SerialName("UnitType") val unitType: Int
)