package com.gv.weather.data.api.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DayNightDetails(
    @SerialName("Icon") val icon: Int,
    @SerialName("IconPhrase") val iconPhrase: String,
    @SerialName("HasPrecipitation") val hasPrecipitation: Boolean,
    @SerialName("PrecipitationType") val precipitationType: String? = null,
    @SerialName("PrecipitationIntensity") val precipitationIntensity: String? = null
)