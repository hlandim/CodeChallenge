package com.arctouch.codechallenge.viewmodel

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


class MoviesViewModel : ViewModel(), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()

    val movies: MutableLiveData<MutableList<Movie>> = MutableLiveData<MutableList<Movie>>().apply { value = mutableListOf() }
    val isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var countPage: Long = 0

    companion object {
        private val movieService = MovieService(ApiService().tmdbApiService)
        private val movieRepository = MovieRepository(movieService)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun load() {
        isLoading.value = true
        requestNextMoviePage()
    }


    fun requestNextMoviePage() {
        countPage++
        upcomingMovies(countPage)
    }

    private fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {

        val response = movieRepository.upcomingMovies(page)
        val dispose = response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (isLoading.value!!) {
                        isLoading.value = false
                    }
                    val moviesWithGenres = it.results.map { movie ->
                        movie.copy(genres = MovieRepository.genres.filter { movie.genreIds?.contains(it.id) == true })
                    }
                    movies.value = moviesWithGenres.toMutableList()
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