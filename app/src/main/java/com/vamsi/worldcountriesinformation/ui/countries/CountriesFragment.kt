package com.vamsi.worldcountriesinformation.ui.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.BaseFragment
import com.vamsi.worldcountriesinformation.core.ClickHandler
import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.core.extensions.inTransaction
import com.vamsi.worldcountriesinformation.databinding.FragmentCountriesBinding
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.ui.countrydetails.CountryDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CountriesAdapter(this)
        binding.apply {
            viewModel = countriesViewModel
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
            countryList.adapter = adapter
        }

        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                countriesViewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is com.vamsi.worldcountriesinformation.domain.core.UiState.Idle -> {
                            // Initial state, do nothing
                        }
                        is com.vamsi.worldcountriesinformation.domain.core.UiState.Loading -> {
                            binding.loading.visibility = android.view.View.VISIBLE
                        }
                        is com.vamsi.worldcountriesinformation.domain.core.UiState.Success -> {
                            binding.loading.visibility = android.view.View.GONE
                            adapter.submitList(uiState.data)
                        }
                        is com.vamsi.worldcountriesinformation.domain.core.UiState.Error -> {
                            binding.loading.visibility = android.view.View.GONE
                            Timber.e(uiState.exception, "Error: ${uiState.message}")
                            // TODO: Show error UI with retry option
                        }
                    }
                }
            }
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