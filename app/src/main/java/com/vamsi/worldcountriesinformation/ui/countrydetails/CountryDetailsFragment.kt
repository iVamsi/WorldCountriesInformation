package com.vamsi.worldcountriesinformation.ui.countrydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.BaseFragment
import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.databinding.FragmentCountryDetailsBinding
import com.vamsi.worldcountriesinformation.domain.countries.Country
import com.vamsi.worldcountriesinformation.domain.countries.CountryDetailsModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CountryDetailsFragment: BaseFragment(), OnMapReadyCallback {
    private val countryDetailsViewModel: CountryDetailsViewModel by viewModels()

    private lateinit var adapter: CountryDetailsAdapter
    private lateinit var binding: FragmentCountryDetailsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    private lateinit var countryDetails: Country

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

        mapView = binding.map.apply {
            onCreate(savedInstanceState)
            onResume()
        }

        mapView.getMapAsync(this)

        val countryDetails = mapCountryDetailsModel(this.arguments)
        adapter.submitList(countryDetails)
    }

    private fun mapCountryDetailsModel(bundle: Bundle?): List<CountryDetailsModel>? {
        return bundle?.let {
            countryDetails = it.getSerializable(Constants.COUNTRY_DETAILS) as Country
            val countryDetailsList = mutableListOf<CountryDetailsModel>()
            countryDetailsList.add(
                CountryDetailsModel(
                    getString(R.string.country_name_label),
                    countryDetails.name
                )
            )
            countryDetailsList.add(
                CountryDetailsModel(
                    getString(R.string.capital_city_label),
                    countryDetails.capital
                )
            )
            return@let countryDetailsList
        } ?: emptyList()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Add a marker and move the camera
        val countryLocation = LatLng(countryDetails.latitude, countryDetails.longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(countryLocation)
                .title("Marker in ${countryDetails.name}")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(countryLocation))
    }
}