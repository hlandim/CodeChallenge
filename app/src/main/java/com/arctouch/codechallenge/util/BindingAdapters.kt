package com.arctouch.codechallenge.util

import android.databinding.BindingAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.ImageView
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.view.home.HomeAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("items")
fun setItems(recyclerView: RecyclerView, list: List<Movie>) {
    recyclerView.adapter.let {
        if (it is HomeAdapter) {
            it.replaceItems(list)
        }
    }
}

@BindingAdapter("posterImageUrl")
fun setPosterImageUrl(view: ImageView, url: String?) {
    if (!TextUtils.isEmpty(url)) {
        Glide.with(view.context)
                .load(MovieImageUrlBuilder.buildPosterUrl(url!!))
                .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                .into(view)
    } else {
        view.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_image_placeholder))
    }
}

@BindingAdapter("backdropImageUrl")
fun setBackdropImageUrl(view: ImageView, url: String?) {
    if (!TextUtils.isEmpty(url)) {
        Glide.with(view.context)
                .load(MovieImageUrlBuilder.buildBackdropUrl(url!!))
                .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                .into(view)
    } else {
        view.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_image_placeholder))
    }
}
