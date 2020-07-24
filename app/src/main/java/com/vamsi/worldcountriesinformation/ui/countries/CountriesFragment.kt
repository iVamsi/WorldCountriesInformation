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
import com.vamsi.worldcountriesinformation.databinding.FragmentCountriesBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CountriesFragment : BaseFragment() {

    private val countriesViewModel: CountriesViewModel by viewModels()
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

        countriesViewModel.countries.observe(viewLifecycleOwner, Observer {
            it.forEach {
                Timber.d(it.name)
            }
        })
    }

}