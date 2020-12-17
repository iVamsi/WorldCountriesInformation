package com.vamsi.worldcountriesinformation.ui.countrydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.BaseFragment
import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.databinding.FragmentCountryDetailsBinding
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountryDetailsModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CountryDetailsFragment: BaseFragment(), OnMapReadyCallback {
    private val countryDetailsViewModel: CountryDetailsViewModel by viewModels()

    private lateinit var adapter: CountryDetailsAdapter
    private lateinit var binding: FragmentCountryDetailsBinding
    private lateinit var googleMap: GoogleMap

    private lateinit var country: Country

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

        val countryDetails = mapCountryDetailsModel(this.arguments)
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)
        adapter = CountryDetailsAdapter()
        binding.apply {
            viewModel = countryDetailsViewModel
            executePendingBindings()
            countryDetailsList.adapter = adapter
        }

        adapter.submitList(countryDetails)
    }

    private fun mapCountryDetailsModel(bundle: Bundle?): List<CountryDetailsModel>? {
        return bundle?.let {
            country = it.getSerializable(Constants.COUNTRY_DETAILS) as Country
            val countryDetailsList = mutableListOf<CountryDetailsModel>()
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.country_name_label),
                    country.name
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.capital_city_label),
                    country.capital
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.population_label),
                    country.population.toString()
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.calling_code_label),
                    country.callingCode
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.language_label),
                    country.languages.map { language -> language.name }.joinToString(", ")
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(getString(R.string.currency_label),
                    country.currencies.map { currency -> currency.name }.joinToString(", ")
                )
            )
            return@let countryDetailsList
        } ?: emptyList()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Add a marker and move the camera
        if (country.latitude != 0.0 && country.longitude != 0.0) {
            val countryLocation = LatLng(country.latitude, country.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(countryLocation)
                    .title("Marker in ${country.name}")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(countryLocation))
        }
    }
}