package com.vamsi.worldcountriesinformation.ui.countries

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.BaseFragment
import com.vamsi.worldcountriesinformation.core.ClickHandler
import com.vamsi.worldcountriesinformation.core.extensions.observe
import com.vamsi.worldcountriesinformation.databinding.FragmentCountriesBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CountriesFragment : BaseFragment(), ClickHandler {

    private val countriesViewModel: CountriesViewModel by viewModels()

    private lateinit var adapter: CountriesAdapter
    private lateinit var binding: FragmentCountriesBinding

    companion object {
        fun newInstance() = CountriesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentCountriesBinding.inflate(inflater, container, false)
            .apply {
                binding = this
            }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = CountriesAdapter(this)
        binding.apply {
            viewModel = countriesViewModel
            executePendingBindings()
            countryList.adapter = adapter
        }

        observe(countriesViewModel.countries) {
            adapter.submitList(it)
        }
    }

    override fun onItemClick(countryCode: String) {
        Timber.d("Item with $countryCode clicked")
    }

}