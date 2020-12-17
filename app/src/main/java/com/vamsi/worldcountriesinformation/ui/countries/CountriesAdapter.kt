package com.vamsi.worldcountriesinformation.ui.countries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vamsi.worldcountriesinformation.core.ClickHandler
import com.vamsi.worldcountriesinformation.databinding.ListItemCountryBinding
import com.vamsi.worldcountriesinformation.domainmodel.Country

/**
 * Adapter for the [RecyclerView] in [CountriesFragment] fragment.
 */
class CountriesAdapter(var listener: ClickHandler) : ListAdapter<Country, CountriesAdapter.ViewHolder>(CountriesDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = getItem(position)
        holder.apply {
            bind(listener, currency)
            itemView.tag = currency
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemCountryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    class ViewHolder(
        private val binding: ListItemCountryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: ClickHandler, item: Country) {
            binding.apply {
                clickListener = listener
                country = item
                executePendingBindings()
            }
        }
    }
}