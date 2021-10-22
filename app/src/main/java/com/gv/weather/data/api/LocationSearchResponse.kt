package com.gv.weather.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LocationSearchResponse(
    @SerialName("Version") val version: Int,
    @SerialName("Key") val key: String,
    @SerialName("Type") val type: String,
    @SerialName("Rank") val rank: Int,
    @SerialName("LocalizedName") val localizedName: String,
    @SerialName("EnglishName") val englishName: String,
    @SerialName("PrimaryPostalCode") val primaryPostalCode: String,
    @SerialName("Region") val region: JsonObject,
    @SerialName("Country") val country: JsonObject,
    @SerialName("AdministrativeArea") val administrativeArea: JsonObject,
    @SerialName("TimeZone") val timeZone: JsonObject,
    @SerialName("GeoPosition") val geoPosition: JsonObject,
    @SerialName("IsAlias") val isAlias: Boolean,
    @SerialName("SupplementalAdminAreas") val supplementalAdminAreas: List<String>,
    @SerialName("DataSets") val dataSets: List<String>
)