package com.arctouch.codechallenge.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.util.Log
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Genre
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.util.Tags
import com.arctouch.codechallenge.util.androidThread
import com.arctouch.codechallenge.util.ioThread
import com.arctouch.codechallenge.web.api.MovieRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import java.util.*


class MoviesViewModel(application: Application, private val movieRepository: MovieRepository) : AndroidViewModel(application), LifecycleObserver, Consumer<Throwable> {

    private val compositeDisposable = CompositeDisposable()

    val movies: MutableLiveData<MutableList<Movie>> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isEmptySearch: MutableLiveData<Boolean> = MutableLiveData()
    private var countPage: Long = 0
    private var isSearchingMode = false
    private var searchQuery: String? = null
    val communicationError = MutableLiveData<String>()
    private var genres = listOf<Genre>()

    init {
        RxJavaPlugins.setErrorHandler(this)
        movies.value = ArrayList()
        isLoading.value = false
        isEmptySearch.value = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun load() {
        if (movies.value!!.isEmpty()) {
            isLoading.value = true
            val dispose = getGenres().subscribeOn(ioThread())
                    .observeOn(androidThread())
                    .subscribe({
                        genres = it.genres
                        requestNextMoviePage()
                    }, {
                        Log.w(Tags.COMMUNICATION_ERROR, it.message)
                        handleCommunicationError(it)
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

    fun getGenres() = movieRepository.getGenres()

    fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {

        val response = movieRepository.upcomingMovies(page)
        val dispose = response
                .subscribeOn(ioThread())
                .observeOn(androidThread())
                .subscribe({

                    handleMovieResponse(it)

                }, {
                    Log.w(Tags.COMMUNICATION_ERROR, it.message)
                    handleCommunicationError(it)
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
        val dispose = response.subscribeOn(ioThread())
                .observeOn(androidThread())
                .subscribe({

                    handleMovieResponse(it)

                }, {
                    Log.w(Tags.COMMUNICATION_ERROR, it.message)
                    handleCommunicationError(it)
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
            movie.copy(genres = genres.filter { movie.genreIds?.contains(it.id) == true })
        }
        val allMovies = movies.value!!.plus(moviesWithGenres).toMutableList()
        isEmptySearch.value = allMovies.isEmpty()
        movies.postValue(allMovies)
    }

    private fun handleCommunicationError(t: Throwable) {
        val message = when (t.cause) {
            is UnknownHostException,
            is IOException -> getApplication<Application>().getString(R.string.network_error)
            is HttpException -> getApplication<Application>().getString(R.string.invalid_parameters_error)
            else -> getApplication<Application>().getString(R.string.unknown_error)
        }

        communicationError.value = message
    }

    override fun onCleared() {
        movieRepository.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

    override fun accept(t: Throwable?) {
        if (t != null) {
            handleCommunicationError(t)
        }
    }


}