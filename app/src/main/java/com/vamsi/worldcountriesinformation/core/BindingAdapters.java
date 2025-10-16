package com.vamsi.worldcountriesinformation.core;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import java.util.Locale;

public class BindingAdapters {
    @BindingAdapter("imageUrl")
    public static void bindImage(AppCompatImageView view, String fileName) {
        if (fileName == null) return;
        String imageUrl = fileName.toLowerCase(Locale.US) + "_flag";
        Glide.with(view.getContext())
            .load(view.getContext()
                .getResources()
                .getIdentifier(imageUrl, "drawable", view.getContext().getPackageName()))
            .into(view);
    }
}
