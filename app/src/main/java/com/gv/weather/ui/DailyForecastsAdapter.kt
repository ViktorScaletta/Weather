package com.gv.weather.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gv.weather.R
import com.gv.weather.core.formatDailyForecastsDate
import com.gv.weather.data.database.details.DayForecast
import com.gv.weather.databinding.RvDailyForecastBinding
import kotlin.math.roundToInt

class DailyForecastsAdapter :
    ListAdapter<DayForecast, DailyForecastsAdapter.ViewHolder>(DiffCallback()) {

    //init { setHasStableIds(true) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvDailyForecastBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    //fun getItem(id: Long): LocationWeather? = currentList.find { it.id == id }

    //override fun getItemId(position: Int): Long = currentList[position].id

    //fun getItemPosition(id: Long): Int = currentList.indexOfFirst { it.id == id }

    inner class ViewHolder (private val binding: RvDailyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(item: DayForecast) {
                with(binding) {
                    date.text = itemView.context.formatDailyForecastsDate(item.date)

                    item.dayWeather.apply {
                        dayWeatherTemperature.text = "${first.roundToInt()}°C"
                        dayWeather.text = second
                        dayWeatherImage.setImageResource(
                            when (second) {
                                "Солнечно" -> R.drawable.sunny
                                "Облачно", "Преим. облачно", "Облачно с прояснениями",
                                "Переменная облачность", "Ночью малооблачно", "Днем малооблачно" ->
                                    R.drawable.cloudy
                                "Дождь", "Ливни" -> R.drawable.rainy
                                "Снег" -> R.drawable.snowy
                                "Снег с дождем" -> R.drawable.rainy_snowy
                                else -> R.drawable.sunny
                            }
                        )
                    }

                    item.nightWeather.apply {
                        nightWeatherTemperature.text = "${first.roundToInt()}°C"
                        nightWeather.text = second
                        nightWeatherImage.setImageResource(
                            when (second) {
                                "Солнечно" -> R.drawable.sunny
                                "Облачно", "Преим. облачно", "Облачно с прояснениями",
                                "Переменная облачность", "Ночью малооблачно", "Днем малооблачно" ->
                                    R.drawable.cloudy
                                "Дождь", "Ливни" -> R.drawable.rainy
                                "Снег" -> R.drawable.snowy
                                "Снег с дождем" -> R.drawable.rainy_snowy
                                else -> R.drawable.sunny
                            }
                        )
                    }
                }
            }

    }

    private class DiffCallback : DiffUtil.ItemCallback<DayForecast>() {
        override fun areItemsTheSame(oldItem: DayForecast, newItem: DayForecast) =
            oldItem == newItem
        override fun areContentsTheSame(oldItem: DayForecast, newItem: DayForecast) =
            oldItem == newItem
    }

}