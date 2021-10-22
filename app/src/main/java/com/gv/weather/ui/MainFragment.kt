package com.gv.weather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.gv.weather.MainViewModel
import com.gv.weather.R
import com.gv.weather.core.collectWithLifecycle
import com.gv.weather.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null
    private val b get() = binding!!

    private var adapter: VPAdapter? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    private val viewModel by activityViewModels<MainViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.locationsWeatherFlow().collectWithLifecycle(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                if (b.tabLayout.tabCount != it.size) applyAdapter()
            } else
                findNavController().navigate(R.id.newLocationDF)
        }

        b.addLocation.setOnClickListener { findNavController().navigate(R.id.newLocationDF) }
    }

    private fun applyAdapter() {
        adapter = VPAdapter(this)
        b.viewPager.adapter = adapter

        tabLayoutMediator =
            TabLayoutMediator(b.tabLayout, b.viewPager) { tab, _ ->
                tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_dynamic_feed)
            }
        tabLayoutMediator?.attach()

        b.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_location)
                        .setPositiveButton("Ok") { _, _ ->
                            tab?.position?.let { viewModel.deleteLocation(it) }
                        }
                        .show()
                }

            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        b.tabLayout.clearOnTabSelectedListeners()
        b.viewPager.adapter = null
        adapter = null
        binding = null
    }

}