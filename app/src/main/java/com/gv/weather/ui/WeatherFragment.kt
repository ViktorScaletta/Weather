package com.gv.weather.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gv.weather.MainViewModel
import com.gv.weather.R
import com.gv.weather.core.collectWithLifecycle
import com.gv.weather.core.formatLastUpdateDate
import com.gv.weather.databinding.FragmentWeatherBinding
import kotlin.math.roundToInt

class WeatherFragment : Fragment() {

    private var binding: FragmentWeatherBinding? = null
    private val b get() = binding!!

    private val viewModel by activityViewModels<MainViewModel>()

    private val key by lazy { arguments?.getString("key") ?: "" }

    private var adapter: DailyForecastsAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return b.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyAdapter()

        viewModel.locationWeatherFlow(key).collectWithLifecycle(viewLifecycleOwner) {
            it?.apply {
                b.city.text = location.city
                b.admAreaAndCountry.text = location.country
                b.temperature.text = "${currentWeather.temperature.roundToInt()}°C"
                b.weather.text = currentWeather.weather
                b.lastUpdateDate.text = getString(R.string.last_update,
                    requireContext().formatLastUpdateDate(lastUpdateDate))
                adapter?.submitList(dailyForecasts)
            }
        }

        val currentLocale =
            ConfigurationCompat.getLocales(resources.configuration)[0].toLanguageTag()
        viewModel.updateWeather(key, currentLocale)
    }

    private fun applyAdapter() {
        adapter = DailyForecastsAdapter()
        b.dailyForecasts.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }

}