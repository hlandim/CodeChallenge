package com.arctouch.codechallenge.view.home

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.databinding.HomeActivityBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.view.base.BaseActivity
import com.arctouch.codechallenge.view.details.MovieDetailsFragment
import com.arctouch.codechallenge.viewmodel.MoviesViewModel
import com.daimajia.androidanimations.library.YoYo
import io.reactivex.disposables.CompositeDisposable


class HomeActivity : BaseActivity(), HomeAdapter.ListListener {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: HomeActivityBinding

    companion object {
        const val FRAGMENT_TAG = "details_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.home_activity)
        binding.lifecycleOwner = this


        val adapter = HomeAdapter(emptyList<Movie>().toMutableList())
        adapter.listener = this
        binding.recyclerView.adapter = adapter
        val viewModel = ViewModelProviders.of(this).get(MoviesViewModel::class.java)
        this.lifecycle.addObserver(viewModel)
        binding.viewModel = viewModel


    }

    override fun onRowClicked(movie: Movie) {
        val detailsFragment = MovieDetailsFragment.newInstance(movie)
        detailsFragment.onFinishAnimationListener = YoYo.AnimatorCallback {
            supportFragmentManager.beginTransaction().remove(detailsFragment).commit()
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
