package com.arctouch.codechallenge.web

import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import io.reactivex.Observable

interface MovieDataSource {

    fun getGenres(): Observable<GenreResponse>

    fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse>

    fun searchMovie(query: String, page: Long): Observable<UpcomingMoviesResponse>
}