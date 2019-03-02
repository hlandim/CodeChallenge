package com.arctouch.codechallenge.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.web.api.ApiService
import com.arctouch.codechallenge.web.api.MovieRepository
import com.arctouch.codechallenge.web.api.MovieService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MoviesViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()

    val movies: MutableLiveData<List<Movie>> = MutableLiveData<List<Movie>>().apply { value = emptyList() }
    val isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    companion object {
        private val movieService = MovieService(ApiService().tmdbApiService)
        private val movieRepository = MovieRepository(movieService)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun load() {
        upcomingMovies(1)
    }


    fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {
        isLoading.value = true
        val response = movieRepository.upcomingMovies(page)
        val dispose = response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isLoading.value = false
                    val moviesWithGenres = it.results.map { movie ->
                        movie.copy(genres = MovieRepository.genres.filter { movie.genreIds?.contains(it.id) == true })
                    }
                    movies.value = moviesWithGenres
                }
        compositeDisposable.add(dispose)

        return response
    }

    override fun onCleared() {
        movieRepository.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }


}