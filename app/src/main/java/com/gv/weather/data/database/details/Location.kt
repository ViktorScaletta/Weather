package com.gv.weather.data.database.details

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    var country: String = "",
    var administrativeArea: String = "",
    var city: String = ""
)