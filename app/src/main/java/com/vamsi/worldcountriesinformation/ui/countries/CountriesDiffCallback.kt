package com.vamsi.worldcountriesinformation.ui.countries

import androidx.recyclerview.widget.DiffUtil
import com.vamsi.worldcountriesinformation.domainmodel.Country

class CountriesDiffCallback : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
        return oldItem == newItem
    }
}