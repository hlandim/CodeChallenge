package com.arctouch.codechallenge.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Genre
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.RxImmediateSchedulerRule
import org.junit.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class MovieDetailsViewModelTest {

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
    fun `Given MovieDetailsViewModel returns genres String, when getGenresString() called`() {
        val movies = mockMovies("Movie 1")

        val movieViewModel = MovieDetailsViewModel(application)
        movieViewModel.movie.value = movies[0]

        val genres = movieViewModel.getGenresString()

        Assert.assertEquals("Genre1, Genre2, Genre3", genres)
    }

    @Test
    fun `Given MovieDetailsViewModel returns release date of a movie, when getReleasedDate() called`() {
        val movies = mockMovies("Movie 1")

        val movieViewModel = MovieDetailsViewModel(application)
        movieViewModel.movie.value = movies[0]

        val releaseDate = movieViewModel.getReleasedDate()
        val matchReleaseDate = application.getString(R.string.released_date).plus("23-02-2019")
        Assert.assertEquals(matchReleaseDate, releaseDate)
    }

    private fun mockMovies(vararg names: String): List<Movie> {
        val genres = mockGenres()
        val genresId = genres.map { genre -> genre.id }
        val mutableList = mutableListOf<Movie>()

        for (name in names) {
            val movie = Movie(1, name, "Overview", genres, genresId, "", "", "23-02-2019")
            mutableList.add(movie)
        }
        return mutableList.toList()
    }

    private fun mockGenres(): MutableList<Genre> {

        val genresList = mutableListOf<Genre>()
        genresList.add(Genre(1, "Genre1"))
        genresList.add(Genre(2, "Genre2"))
        genresList.add(Genre(3, "Genre3"))

        return genresList
    }


}