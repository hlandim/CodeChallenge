package com.arctouch.codechallenge.web.api

import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    companion object {
        private const val URL = "https://api.themoviedb.org/3/"
        const val API_KEY = "1f54bd990f1cdfb230adb312546d765d"
        const val DEFAULT_LANGUAGE = "pt-BR"
        const val DEFAULT_REGION = ""

        fun create(): TmdbApi {
            val retrofit = Retrofit.Builder()
                    .baseUrl(URL)
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return retrofit.create(TmdbApi::class.java)
        }
    }

    @GET("genre/movie/list")
    fun genres(
            @Query("api_key") apiKey: String = API_KEY,
            @Query("language") language: String = DEFAULT_LANGUAGE
    ): Observable<GenreResponse>

    @GET("movie/upcoming")
    fun upcomingMovies(
            @Query("api_key") apiKey: String = API_KEY,
            @Query("language") language: String = DEFAULT_LANGUAGE,
            @Query("page") page: Long,
            @Query("region") region: String = DEFAULT_REGION
    ): Observable<UpcomingMoviesResponse>
}
