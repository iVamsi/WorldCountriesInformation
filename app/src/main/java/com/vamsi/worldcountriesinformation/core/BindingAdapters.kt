package com.vamsi.worldcountriesinformation.core

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.util.Locale

object BindingAdapters {
    @BindingAdapter("imageUrl")
    @JvmStatic fun bindImage(view: AppCompatImageView, fileName: String) {
        val imageUrl = fileName.toLowerCase(Locale.US).plus("_flag")
        Glide.with(view.context).load(view.context
            .resources
            .getIdentifier(imageUrl, "drawable", view.context.packageName))
            .into(view)
    }
}