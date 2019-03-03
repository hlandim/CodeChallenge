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

    var compositeDisposable = CompositeDisposable()


    override fun getGenres(): Observable<GenreResponse> {
        val response = dataSource.getGenres()
        val disposable = response.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    //TODO Save in databese
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