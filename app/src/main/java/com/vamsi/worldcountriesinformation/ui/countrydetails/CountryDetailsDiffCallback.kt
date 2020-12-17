package com.vamsi.worldcountriesinformation.ui.countrydetails

import androidx.recyclerview.widget.DiffUtil
import com.vamsi.worldcountriesinformation.domainmodel.CountryDetailsModel

class CountryDetailsDiffCallback : DiffUtil.ItemCallback<CountryDetailsModel>() {
    override fun areItemsTheSame(oldItem: CountryDetailsModel, newItem: CountryDetailsModel): Boolean {
        return oldItem.value == newItem.value
    }

    override fun areContentsTheSame(oldItem: CountryDetailsModel, newItem: CountryDetailsModel): Boolean {
        return oldItem == newItem
    }
}