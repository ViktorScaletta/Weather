package com.gv.weather.data

import com.gv.weather.core.apiKey3
import com.gv.weather.core.currentDateTime
import com.gv.weather.core.formatPhrase
import com.gv.weather.core.serverDateToNormal
import com.gv.weather.data.api.CurrentWeatherResponse
import com.gv.weather.data.api.DailyWeatherResponse
import com.gv.weather.data.api.LocationSearchResponse
import com.gv.weather.data.database.LocationWeather
import com.gv.weather.data.database.MainDao
import com.gv.weather.data.database.details.CurrentWeather
import com.gv.weather.data.database.details.DayForecast
import com.gv.weather.data.database.details.Location
import com.gv.weather.domain.Repository
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.java.KoinJavaComponent.inject

class RepositoryImpl(private val dao: MainDao) : Repository {

    override val client: HttpClient by inject(HttpClient::class.java)

    override fun locationsWeatherFlow() = dao.locationsWeatherFlow().distinctUntilChanged()

    override fun locationWeatherFlow(key: String) =
        dao.locationWeatherFlow(key).distinctUntilChanged()

    override fun locationWeather(key: String) = dao.locationWeather(key)

    override suspend fun updateWeather(key: String, lang: String) {
        locationWeather(key)?.apply {
            if (lastUpdateDate == null ||
                currentDateTime.time.minus(lastUpdateDate?.time ?: 0) > 3600000) {
                val currentWeatherResponse: List<CurrentWeatherResponse> =
                    client.get("http://dataservice.accuweather.com/currentconditions/v1/$key") {
                        parameter("apikey", apiKey3)
                        parameter("language", lang)
                    }
                val dailyForecastsResponse: DailyWeatherResponse =
                    client.get("http://dataservice.accuweather.com/forecasts/v1/daily/5day/$key") {
                        parameter("apikey", apiKey3)
                        parameter("language", lang)
                        parameter("metric", true)
                    }

                currentWeather =
                    CurrentWeather(
                        currentWeatherResponse[0].temperature.metric.value,
                        currentWeatherResponse[0].weatherText
                    )
                dailyForecasts = dailyForecastsResponse.dailyForecasts.map { it.run {
                    DayForecast(
                        date.serverDateToNormal("yyyy-MM-dd'T'HH:mm:ssX"),
                        Pair(dailyForecastTemperature.maximum.value, day.iconPhrase.formatPhrase()),
                        Pair(dailyForecastTemperature.minimum.value, night.iconPhrase.formatPhrase())
                    )
                } }
                lastUpdateDate = currentDateTime
                dao.update(this)
            }
        }
    }

    override suspend fun obtainLocations(query: String, lang: String): List<LocationSearchItem> {
        val result: List<LocationSearchResponse> =
            client.get("http://dataservice.accuweather.com/locations/v1/cities/search") {
                parameter("apikey", apiKey3)
                parameter("q", query)
                parameter("language", lang)
            }
        return result.map {
            LocationSearchItem(
                it.key,
                it.localizedName,
                it.administrativeArea["LocalizedName"]?.toString()?.trim('"') ?: "",
                it.country["LocalizedName"]?.toString()?.trim('"') ?: ""
            )
        }
    }

    override suspend fun addLocation(locationSearchItem: LocationSearchItem) {
        if (dao.locationWeather(locationSearchItem.key) == null)
            locationSearchItem.run {
                LocationWeather(key, Location(country, administrativeArea, name))
                    .let { dao.insert(it) }
            }
    }

    override suspend fun deleteLocation(position: Int) {
        dao.locationsWeather().getOrNull(position)?.also { dao.delete(it) }
    }

}