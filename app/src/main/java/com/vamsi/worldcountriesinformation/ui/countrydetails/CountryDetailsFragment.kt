package com.vamsi.worldcountriesinformation.ui.countrydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.BaseFragment
import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.databinding.FragmentCountryDetailsBinding
import com.vamsi.worldcountriesinformation.domain.countries.Country
import com.vamsi.worldcountriesinformation.domain.countries.CountryDetailsModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryDetailsFragment: BaseFragment() {
    private val countryDetailsViewModel: CountryDetailsViewModel by viewModels()

    private lateinit var adapter: CountryDetailsAdapter
    private lateinit var binding: FragmentCountryDetailsBinding

    companion object {
        fun newInstance(bundle: Bundle) = CountryDetailsFragment().apply {
            this.arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentCountryDetailsBinding.inflate(inflater, container, false)
            .apply {
                binding = this
            }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = CountryDetailsAdapter()
        binding.apply {
            viewModel = countryDetailsViewModel
            executePendingBindings()
            countryDetailsList.adapter = adapter
        }

        val countryDetails = mapCountryDetailsModel(this.arguments)
        adapter.submitList(countryDetails)
    }

    private fun mapCountryDetailsModel(bundle: Bundle?): List<CountryDetailsModel>? {
        return bundle?.let {
            val countryDetails = bundle.getSerializable(Constants.COUNTRY_DETAILS) as Country
            val countryDetailsList = mutableListOf<CountryDetailsModel>()
            countryDetailsList.add(CountryDetailsModel(getString(R.string.country_name_label), countryDetails.name))
            countryDetailsList.add(CountryDetailsModel(getString(R.string.capital_city_label), countryDetails.capital))
            return@let countryDetailsList
        } ?: emptyList()
    }
}