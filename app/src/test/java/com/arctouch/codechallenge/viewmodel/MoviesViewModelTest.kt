package com.arctouch.codechallenge.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.arctouch.codechallenge.model.Genre
import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import com.arctouch.codechallenge.util.RxImmediateSchedulerRule
import com.arctouch.codechallenge.web.api.MovieRepository
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class MoviesViewModelTest {

    @Mock
    lateinit var movieDataRepository: MovieRepository

    @Mock
    lateinit var application: Application

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Given MovieRepository returns next Movies, when upcomingMovies() called, then update live data`() {
        val movies = mockMovies("Movie 1","Movie 2","Movie 3","Movie 4" )
        val upcomingMoviesResponse = UpcomingMoviesResponse(movies.size, movies, 1, movies.size)
        whenever(movieDataRepository.upcomingMovies(1)).thenReturn(Observable.just(upcomingMoviesResponse))

        val movieViewModel = MoviesViewModel(application, movieDataRepository)


        val genres = GenreResponse(mockGenres())

        whenever(movieViewModel.getGenres()).thenReturn(Observable.just(genres))

        movieViewModel.upcomingMovies(1)

        Assert.assertEquals(movies, movieViewModel.movies.value)
    }

    @Test
    fun `Given MovieRepository returns Movies, when load() called, then update live data`() {
        val movies = mockMovies()
        val upcomingMoviesResponse = UpcomingMoviesResponse(movies.size, movies, 1, movies.size)
        whenever(movieDataRepository.upcomingMovies(1)).thenReturn(Observable.just(upcomingMoviesResponse))

        val movieViewModel = MoviesViewModel(application, movieDataRepository)


        val genres = GenreResponse(mockGenres())

        whenever(movieViewModel.getGenres()).thenReturn(Observable.just(genres))

        movieViewModel.load()

        Assert.assertEquals(movies, movieViewModel.movies.value)
    }

    @Test
    fun `Given MovieRepository returns Movies from a search, when searchMovie() called, then update live data`() {
        val movieName = "Movie 1"
        val movies = mockMovies(movieName)
        val upcomingMoviesResponse = UpcomingMoviesResponse(movies.size, movies, 1, movies.size)
        whenever(movieDataRepository.searchMovie(movieName, 1)).thenReturn(Observable.just(upcomingMoviesResponse))

        val movieViewModel = MoviesViewModel(application, movieDataRepository)

        val genres = GenreResponse(mockGenres())

        whenever(movieViewModel.getGenres()).thenReturn(Observable.just(genres))


        movieViewModel.searchMovie(movieName)

        Assert.assertEquals(1, movieViewModel.movies.value?.size)
        Assert.assertEquals(movieName, movieViewModel.movies.value?.get(0)?.title)

    }

    private fun mockMovies(vararg names: String): List<Movie> {
        val genres = mockGenres()
        val genresId = genres.map { genre -> genre.id }
        val mutableList = mutableListOf<Movie>()

        for (name in names) {
            val movie = Movie(1, name, "Overview", genres, genresId, "", "", "")
            mutableList.add(movie)
        }
        return mutableList.toList()
    }

    private fun mockGenres(): MutableList<Genre> {

        val genresList = mutableListOf<Genre>()
        genresList.add(Genre(1, "Genre1"))
        genresList.add(Genre(2, "Genre1"))
        genresList.add(Genre(3, "Genre2"))

        return genresList
    }
}