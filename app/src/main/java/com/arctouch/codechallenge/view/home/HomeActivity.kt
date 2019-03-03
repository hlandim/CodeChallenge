package com.arctouch.codechallenge.view.home

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.databinding.HomeActivityBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.ConnectivityReceiver
import com.arctouch.codechallenge.util.Tags
import com.arctouch.codechallenge.util.getViewModel
import com.arctouch.codechallenge.view.details.MovieDetailsFragment
import com.arctouch.codechallenge.viewmodel.MoviesViewModel
import com.arctouch.codechallenge.web.api.MovieRepository
import com.arctouch.codechallenge.web.api.MovieService
import com.arctouch.codechallenge.web.api.TmdbApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class HomeActivity : AppCompatActivity(), HomeAdapter.ListListener, ConnectivityReceiver.ConnectivityReceiverListener {

    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var mBinding: HomeActivityBinding
    private lateinit var mViewModel: MoviesViewModel
    private var mIsLoading = false
    private val mConnectivityReceiver = ConnectivityReceiver(this)
    private var snackBar: Snackbar? = null

    companion object {
        const val FRAGMENT_TAG = "details_fragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = createViewModel()
        configureDataBinding()
        registerReceiver(mConnectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun configureDataBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.home_activity)
        mBinding.lifecycleOwner = this

        val adapter = HomeAdapter(emptyList<Movie>().toMutableList())
        adapter.listener = this
        mBinding.recyclerView.adapter = adapter

        this.lifecycle.addObserver(mViewModel)
        mBinding.viewModel = mViewModel

        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView!!.canScrollVertically(1)
                        && !mIsLoading) {
                    mBinding.recyclerView.post { adapter.showLoading() }
                    val dispose = mViewModel.requestNextPage().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                adapter.hideLoading()
                            }

                    mCompositeDisposable.add(dispose)


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
            mViewModel.searchMovie(query)
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
                mViewModel.resetSearchVariables()
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
            this.mIsLoading = isModelViewLoading!!
        })

        return viewModel
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {

        var message = getString(R.string.connection_off)
        var color = R.color.red
        if (isConnected) {
            message = getString(R.string.connection_on)
            color = R.color.green
        }

        if (snackBar != null) {
            snackBar?.view?.setBackgroundColor(ContextCompat.getColor(this, color))
            snackBar?.setText(message)
            snackBar?.show()
        } else {
            snackBar = Snackbar.make(mBinding.root, message, Snackbar.LENGTH_LONG)
        }
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
        try {
            unregisterReceiver(mConnectivityReceiver)
        } catch (e: Exception) {
            Log.w(Tags.HANDLED_EXCEPTION, e.printStackTrace().toString())
        }
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}
