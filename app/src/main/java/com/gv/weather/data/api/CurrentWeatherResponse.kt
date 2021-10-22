package com.gv.weather.data.api

import com.gv.weather.data.api.details.CurrentWeatherTemperature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherResponse(
    @SerialName("LocalObservationDateTime") val localObservationDateTime: String,
    @SerialName("EpochTime") val epochTime: Int,
    @SerialName("WeatherText") val weatherText: String,
    @SerialName("WeatherIcon") val weatherIcon: Int,
    @SerialName("HasPrecipitation") val hasPrecipitation: Boolean,
    @SerialName("PrecipitationType") val precipitationType: String? = null,
    @SerialName("IsDayTime") val isDayTime: Boolean,
    @SerialName("Temperature") val temperature: CurrentWeatherTemperature,
    @SerialName("MobileLink") val mobileLink: String,
    @SerialName("Link") val link: String
)