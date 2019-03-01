package com.arctouch.codechallenge.view.home

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.databinding.HomeActivityBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.view.base.BaseActivity
import com.arctouch.codechallenge.viewmodel.MovieViewModel
import io.reactivex.disposables.CompositeDisposable

class HomeActivity : BaseActivity(), HomeAdapter.RowClickListener {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: HomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.home_activity)
        binding.lifecycleOwner = this


        val adapter = HomeAdapter(emptyList())
        adapter.listener = this
        binding.recyclerView.adapter = adapter

        val viewModel = ViewModelProviders.of(this).get(MovieViewModel::class.java)
        this.lifecycle.addObserver(viewModel)
        binding.viewModel = viewModel

    }

    override fun onRowClicked(movie: Movie) {
        Toast.makeText(this, movie.title, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
