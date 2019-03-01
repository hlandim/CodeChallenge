package com.arctouch.codechallenge.util

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.view.home.HomeAdapter

@BindingAdapter("items")
fun setItems(recyclerView: RecyclerView, list: List<Movie>) {
    recyclerView.adapter.let {
        if (it is HomeAdapter) {
            it.replaceItems(list)
        }
    }
}
