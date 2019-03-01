package com.arctouch.codechallenge.web.api

import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.web.MovieDataSource
import io.reactivex.Observable

class MovieService(private val api: TmdbApi) : MovieDataSource {

    override fun getGenres(): Observable<GenreResponse> {
        return api.genres(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE)
    }

    override fun upcomingMovies(page: Long): Observable<UpcomingMoviesResponse> {
        return api.upcomingMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, page, TmdbApi.DEFAULT_REGION)
    }

}