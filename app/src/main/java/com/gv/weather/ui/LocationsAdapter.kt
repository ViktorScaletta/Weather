package com.gv.weather.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gv.weather.data.LocationSearchItem
import com.gv.weather.databinding.RvSearchItemBinding

class LocationsAdapter(val onClick: LocationSearchItem.() -> Unit) :
    ListAdapter<LocationSearchItem, LocationsAdapter.ViewHolder>(DiffCallback()) {

    //init { setHasStableIds(true) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvSearchItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    //fun getItem(id: Long): LocationWeather? = currentList.find { it.id == id }

    //override fun getItemId(position: Int): Long = currentList[position].id

    //fun getItemPosition(id: Long): Int = currentList.indexOfFirst { it.id == id }

    inner class ViewHolder (private val binding: RvSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(item: LocationSearchItem) {
                with(binding) {
                    root.setOnClickListener { onClick(item) }

                    locationName.text = item.name
                    locationArea.text = "${item.administrativeArea}, ${item.country}"
                }
            }

    }

    private class DiffCallback : DiffUtil.ItemCallback<LocationSearchItem>() {
        override fun areItemsTheSame(oldItem: LocationSearchItem, newItem: LocationSearchItem) =
            oldItem == newItem
        override fun areContentsTheSame(oldItem: LocationSearchItem, newItem: LocationSearchItem) =
            oldItem == newItem
    }

}