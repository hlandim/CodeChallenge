package com.arctouch.codechallenge.view.home

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.databinding.HomeActivityBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.getViewModel
import com.arctouch.codechallenge.view.details.MovieDetailsFragment
import com.arctouch.codechallenge.viewmodel.MoviesViewModel
import com.arctouch.codechallenge.web.api.MovieRepository
import com.arctouch.codechallenge.web.api.MovieService
import com.arctouch.codechallenge.web.api.TmdbApi
import com.daimajia.androidanimations.library.YoYo
import io.reactivex.disposables.CompositeDisposable


class HomeActivity : AppCompatActivity(), HomeAdapter.ListListener {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: HomeActivityBinding
    private var isLoading = false

    companion object {
        const val FRAGMENT_TAG = "details_fragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movieService = MovieService(TmdbApi.create())
        val movieRepository = MovieRepository(movieService)

        binding = DataBindingUtil.setContentView(this, R.layout.home_activity)
        binding.lifecycleOwner = this

        val adapter = HomeAdapter(emptyList<Movie>().toMutableList())
        adapter.listener = this
        binding.recyclerView.adapter = adapter
        val viewModel: MoviesViewModel by lazy {
            getViewModel { MoviesViewModel(movieRepository) }
        }
        this.lifecycle.addObserver(viewModel)
        binding.viewModel = viewModel
        viewModel.communicationErro.observe(this, Observer { message ->
            Snackbar.make(binding.rootLayout, message.toString(), Snackbar.LENGTH_LONG).show()
        })

        binding.viewModel!!.isLoading.observe(this, Observer { isModelViewLoading ->
            this.isLoading = isModelViewLoading!!
        })

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView!!.canScrollVertically(1)
                        && !isLoading) {
                    test()
                    binding.viewModel!!.requestNextMoviePage()

                }
            }
        })
    }

    fun test() {
        Toast.makeText(this, "Last", Toast.LENGTH_SHORT).show()
    }


    override fun onRowClicked(movie: Movie) {
        val detailsFragment = MovieDetailsFragment.newInstance(movie)
        detailsFragment.onFinishAnimationListener = YoYo.AnimatorCallback {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
            supportFragmentManager.beginTransaction().remove(fragment).addToBackStack(null).commit()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_details, detailsFragment, FRAGMENT_TAG).commit()

    }

    override fun onBottomReached() {
        binding.viewModel!!.requestNextMoviePage()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment != null) {
            (fragment as MovieDetailsFragment).startCloseAnimation()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
