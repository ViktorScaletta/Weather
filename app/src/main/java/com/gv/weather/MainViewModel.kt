package com.gv.weather

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gv.weather.core.getSharedPrefs
import com.gv.weather.data.LocationSearchItem
import com.gv.weather.domain.Interactor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val interactor: Interactor by inject(Interactor::class.java)

    fun locationsWeatherFlow() = interactor.locationsWeatherFlow().flowOn(Dispatchers.IO)

    fun locationWeatherFlow(key: String) = interactor.locationWeatherFlow(key).flowOn(Dispatchers.IO)

    private val prefs by lazy { app.getSharedPrefs("MainSettings") }

    private var tabs
        get() = prefs.getString("tabs", null)?.split(";")
            ?.map { it.trim('(', ')')
                .run { Pair(substringBefore(",").toInt(), substringAfter(",")) } }
            ?: emptyList()
        set(value) = prefs.edit(true) {
            putString("tabs", value.joinToString(";").filter { !it.isWhitespace() })
        }

    fun updateWeather(key: String, lang: String) = viewModelScope.launch(Dispatchers.IO) {
        interactor.updateWeather(key, lang)
    }

    suspend fun obtainLocations(query: String, lang: String) =
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            interactor.obtainLocations(query, lang)
        }

    fun addLocation(locationSearchItem: LocationSearchItem) =
        viewModelScope.launch(Dispatchers.IO) {
            tabs = tabs + Pair(tabs.size, locationSearchItem.key)
            interactor.addLocation(locationSearchItem)
        }

    fun deleteLocation(position: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            tabs.getOrNull(position)?.let { tabs = tabs - it }
            interactor.deleteLocation(position)
        }

}