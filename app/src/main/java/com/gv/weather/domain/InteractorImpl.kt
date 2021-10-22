package com.gv.weather.domain

import com.gv.weather.data.LocationSearchItem

class InteractorImpl(private val repository: Repository): Interactor {

    override fun locationsWeatherFlow() = repository.locationsWeatherFlow()

    override fun locationWeatherFlow(key: String) = repository.locationWeatherFlow(key)

    override suspend fun updateWeather(key: String, lang: String) =
        repository.updateWeather(key, lang)

    override suspend fun obtainLocations(query: String, lang: String) =
        repository.obtainLocations(query, lang)

    override suspend fun addLocation(locationSearchItem: LocationSearchItem) =
        repository.addLocation(locationSearchItem)

    override suspend fun deleteLocation(position: Int) =
        repository.deleteLocation(position)

}