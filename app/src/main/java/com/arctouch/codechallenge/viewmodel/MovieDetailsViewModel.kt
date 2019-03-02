package com.arctouch.codechallenge.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.arctouch.codechallenge.model.Movie

class MovieDetailsViewModel : ViewModel() {

    var movie: MutableLiveData<Movie> = MutableLiveData()

    fun getGenresString(): String? {
        return movie.value?.genres?.joinToString(separator = ", ") { it.name }
    }

}