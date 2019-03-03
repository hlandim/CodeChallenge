package com.arctouch.codechallenge.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Movie

class MovieDetailsViewModel(application: Application) : AndroidViewModel(application) {

    var movie: MutableLiveData<Movie> = MutableLiveData()

    fun getGenresString() = movie.value?.genres?.joinToString(separator = ", ") { it.name }

    fun getReleasedDate() = getApplication<Application>().getString(R.string.released_date) + movie.value?.releaseDate

}