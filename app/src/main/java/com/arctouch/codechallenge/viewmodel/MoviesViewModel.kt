package com.arctouch.codechallenge.viewmodel

import android.arch.lifecycle.*
import android.util.Log
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.util.Tags
import com.arctouch.codechallenge.web.api.MovieRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MoviesViewModel(private val movieRepository: MovieRepository) : ViewModel(), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()

    val movies: MutableLiveData<MutableList<Movie>> = MutableLiveData<MutableList<Movie>>().apply { value = mutableListOf() }
    val isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var countPage: Long = 0
    val communicationErro = MutableLiveData<String>()


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun load() {
        if (movies.value!!.isEmpty()) {
            isLoading.value = true
            val dispose = getGenres().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        requestNextMoviePage()
                    }, {
                        Log.w(Tags.COMMUNICATION_ERROR, it.message)
                        communicationErro.value = it.message
                    })

            compositeDisposable.add(dispose)
        }
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
                .subscribe({
                    if (isLoading.value!!) {
                        isLoading.value = false
                    }
                    val moviesWithGenres = it.results.map { movie ->
                        movie.copy(genres = MovieRepository.genres.filter { movie.genreIds?.contains(it.id) == true })
                    }
                    val allMovies = movies.value!!.plus(moviesWithGenres).toMutableList()
                    movies.postValue(allMovies)

                }, {
                    Log.w(Tags.COMMUNICATION_ERROR, it.message)
                    communicationErro.value = it.message
                })
        compositeDisposable.add(dispose)

        return response
    }

    private fun getGenres() = movieRepository.getGenres()


    override fun onCleared() {
        movieRepository.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }


}