package com.arctouch.codechallenge.view.home

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.databinding.HomeActivityBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.getViewModel
import com.arctouch.codechallenge.view.details.MovieDetailsFragment
import com.arctouch.codechallenge.viewmodel.MoviesViewModel
import com.arctouch.codechallenge.web.api.MovieRepository
import com.arctouch.codechallenge.web.api.MovieService
import com.arctouch.codechallenge.web.api.TmdbApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException


class HomeActivity : AppCompatActivity(), HomeAdapter.ListListener, Consumer<Throwable> {


    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: HomeActivityBinding
    private lateinit var viewModel: MoviesViewModel
    private var isLoading = false

    companion object {
        const val FRAGMENT_TAG = "details_fragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxJavaPlugins.setErrorHandler(this)

        viewModel = createViewModel()
        configureDataBinding()

    }

    private fun configureDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.home_activity)
        binding.lifecycleOwner = this

        val adapter = HomeAdapter(emptyList<Movie>().toMutableList())
        adapter.listener = this
        binding.recyclerView.adapter = adapter

        this.lifecycle.addObserver(viewModel)
        binding.viewModel = viewModel

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView!!.canScrollVertically(1)
                        && !isLoading) {
                    binding.recyclerView.post { adapter.showLoading() }
                    val dispose = viewModel.requestNextPage().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                adapter.hideLoading()
                            }

                    compositeDisposable.add(dispose)


                }
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
//            Toast.makeText(this, query, Toast.LENGTH_SHORT).show()
            viewModel.searchMovie(query)
            //use the query to search your data somehow

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.resetSearchVariables()
                return true
            }

        })

        return true
    }

    private fun createViewModel(): MoviesViewModel {

        val movieService = MovieService(TmdbApi.create())
        val movieRepository = MovieRepository(movieService)

        val viewModel: MoviesViewModel by lazy {
            getViewModel { MoviesViewModel(application, movieRepository) }
        }

        viewModel.isLoading.observe(this, Observer { isModelViewLoading ->
            this.isLoading = isModelViewLoading!!
        })

        return viewModel
    }

    override fun onRowClicked(movie: Movie) {
        val detailsFragment = MovieDetailsFragment.newInstance(movie)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_details, detailsFragment, FRAGMENT_TAG).commit()

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

    override fun accept(t: Throwable) {


        val message = when (t.cause) {
            is UnknownHostException,
            is IOException -> {
                getString(R.string.network_error)
            }
            is HttpException -> getString(R.string.invalid_parameters_error)
            else -> {
                getString(R.string.unknown_error)
            }
        }

        viewModel.communicationError.value = message
        val finalMessage = message + ": " + t.message
        val snackBar = Snackbar.make(binding.rootLayout, finalMessage, Snackbar.LENGTH_LONG)
        snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        snackBar.show()
    }
}
