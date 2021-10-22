package com.gv.weather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.gv.weather.MainViewModel
import com.gv.weather.data.LocationSearchItem
import com.gv.weather.databinding.FragmentNewLocationDialogBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewLocationDialogFragment : DialogFragment() {

    private var binding: FragmentNewLocationDialogBinding? = null
    private val b get() = binding!!

    private val viewModel by activityViewModels<MainViewModel>()

    private var adapter: LocationsAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewLocationDialogBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().window?.setLayout(MATCH_PARENT, WRAP_CONTENT)

        applyAdapter()

        applyListeners()

        /*val searchManager = requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        b.searchView.apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
        }*/
    }

    private fun applyAdapter() {
        adapter = LocationsAdapter { onClick() }
        b.resultsRV.adapter = adapter
    }

    private fun applyListeners() {
        val currentLocale =
            ConfigurationCompat.getLocales(resources.configuration)[0].toLanguageTag()

        var searchJob: Job? = null

        b.searchButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    val data = viewModel.obtainLocations(b.searchET.text.toString(), currentLocale)
                    adapter?.submitList(data)
                }
            }
        }
    }

    private fun LocationSearchItem.onClick() {
        viewModel.addLocation(this)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }

}