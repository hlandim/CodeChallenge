package com.arctouch.codechallenge.viewmodel

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.web.api.ApiService
import com.arctouch.codechallenge.web.api.MovieRepository
import com.arctouch.codechallenge.web.api.MovieService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class GenreViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        private val movieService = MovieService(ApiService().tmdbApiService)
        private val movieRepository = MovieRepository(movieService)
    }

    fun getGenres(): Observable<GenreResponse> {
        val response = movieRepository
                .getGenres()
        val dispose = response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("TESTE", "2")
                }

        compositeDisposable.add(dispose)

        return response
    }

    override fun onCleared() {
        Log.d("TESTE", "onCleared")
        compositeDisposable.dispose()
        super.onCleared()
    }
}