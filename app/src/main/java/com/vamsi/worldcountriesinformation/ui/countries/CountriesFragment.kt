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
import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.core.extensions.inTransaction
import com.vamsi.worldcountriesinformation.core.extensions.observe
import com.vamsi.worldcountriesinformation.databinding.FragmentCountriesBinding
import com.vamsi.worldcountriesinformation.domain.countries.Country
import com.vamsi.worldcountriesinformation.ui.countrydetails.CountryDetailsFragment
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

    override fun onItemClick(country: Country) {
        Timber.d("Item with ${country.threeLetterCode} clicked")
        activity?.supportFragmentManager?.inTransaction {
            replace(R.id.fragment_container, CountryDetailsFragment.newInstance(Bundle().apply { putSerializable(Constants.COUNTRY_DETAILS, country) }))
            addToBackStack(CountryDetailsFragment::class.java.simpleName)
        }
    }

}