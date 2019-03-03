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
    val isEmptySearch: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    private var countPage: Long = 0
    private var isSearchingMode = false
    private var searchQuery: String? = null
    val communicationError = MutableLiveData<Throwable>()

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
                        communicationError.value = it
                    })

            compositeDisposable.add(dispose)
        }
    }

    fun requestNextPage(): Observable<UpcomingMoviesResponse> {
        return if (isSearchingMode && searchQuery != null) {
            requestNextSearchPage()
        } else {
            requestNextMoviePage()
        }
    }

    private fun requestNextMoviePage(): Observable<UpcomingMoviesResponse> {
        countPage++
        return upcomingMovies(countPage)
    }

    private fun getGenres() = movieRepository.getGenres()

    private fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {

        val response = movieRepository.upcomingMovies(page)
        val dispose = response
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    handleMovieResponse(it)

                }, {
                    Log.w(Tags.COMMUNICATION_ERROR, it.message)
                    communicationError.value = it
                })
        compositeDisposable.add(dispose)

        return response
    }

    private fun requestNextSearchPage(): Observable<UpcomingMoviesResponse> {
        return searchMovie(searchQuery!!)
    }

    fun searchMovie(query: String): Observable<UpcomingMoviesResponse> {
        if (query != searchQuery) {
            countPage = 1
            movies.value?.clear()
        } else if (searchQuery == null) {
            countPage = 1
        }
        isLoading.value = true
        isSearchingMode = true
        searchQuery = query
        val response = movieRepository.searchMovie(query, countPage)
        val dispose = response.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    handleMovieResponse(it)

                }, {
                    Log.w(Tags.COMMUNICATION_ERROR, it.message)
                    communicationError.value = it
                })
        compositeDisposable.add(dispose)

        return response
    }

    fun resetSearchVariables() {
        if (isSearchingMode) {
            countPage = 0
            isSearchingMode = false
        }
        searchQuery = null
    }

    private fun handleMovieResponse(response: UpcomingMoviesResponse) {
        if (isLoading.value!!) {
            isLoading.value = false
        }
        val moviesWithGenres = response.results.map { movie ->
            movie.copy(genres = MovieRepository.genres.filter { movie.genreIds?.contains(it.id) == true })
        }
        val allMovies = movies.value!!.plus(moviesWithGenres).toMutableList()
        isEmptySearch.value = allMovies.isEmpty()
        movies.postValue(allMovies)
    }

    override fun onCleared() {
        movieRepository.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }


}