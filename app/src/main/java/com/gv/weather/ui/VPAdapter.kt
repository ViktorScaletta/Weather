package com.gv.weather.ui

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.gv.weather.data.ViewPagerAdapter

class VPAdapter(fragment: Fragment) : ViewPagerAdapter(fragment) {

    override fun getItemCount() = tabs.size

    override fun createFragment(position: Int): Fragment =
        WeatherFragment().apply {
            tabs.find { it.first == position }?.let { arguments = bundleOf("key" to it.second) } }
    
}