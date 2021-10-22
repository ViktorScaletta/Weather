package com.gv.weather.data.api.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyForecast(
    @SerialName("Date") val date: String,
    @SerialName("EpochDate") val epochDate: Int,
    @SerialName("Temperature") val dailyForecastTemperature: DailyForecastTemperature,
    @SerialName("Day") val day: DayNightDetails,
    @SerialName("Night") val night: DayNightDetails,
    @SerialName("Sources") val sources: List<String>? = null,
    @SerialName("MobileLink") val mobileLink: String,
    @SerialName("Link") val link: String
)