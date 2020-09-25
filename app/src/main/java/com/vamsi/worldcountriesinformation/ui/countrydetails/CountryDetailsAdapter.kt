package com.vamsi.worldcountriesinformation.ui.countrydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vamsi.worldcountriesinformation.databinding.ListItemCountryDetailBinding
import com.vamsi.worldcountriesinformation.domain.countries.CountryDetailsModel

class CountryDetailsAdapter: ListAdapter<CountryDetailsModel, CountryDetailsAdapter.ViewHolder>(
    CountryDetailsDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = getItem(position)
        holder.apply {
            bind(currency)
            itemView.tag = currency
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemCountryDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    class ViewHolder(
        private val binding: ListItemCountryDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CountryDetailsModel) {
            binding.apply {
                country = item
                executePendingBindings()
            }
        }
    }
}