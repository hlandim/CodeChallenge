package com.arctouch.codechallenge.web.api

import android.util.Log
import com.arctouch.codechallenge.model.Genre
import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.web.MovieDataSource
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MovieRepository(private val dataSource: MovieDataSource) : MovieDataSource {

    companion object {
        var genres = listOf<Genre>()
    }


    var compositeDisposable = CompositeDisposable()

    private fun cacheGenres(genres: List<Genre>) {
        MovieRepository.genres = genres
    }

    override fun getGenres(): Observable<GenreResponse> {
        val response = dataSource.getGenres()
        val disposable = response.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("TESTE", "3")
                    cacheGenres(it.genres)
                }
        compositeDisposable.add(disposable)

        return response
    }

    override fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {
        return dataSource.upcomingMovies(page)
    }

    override fun searchMovie(query: String, page: Long): Observable<UpcomingMoviesResponse> {
        return dataSource.searchMovie(query, page)
    }


    fun dispose() {
        compositeDisposable.dispose()
    }
}