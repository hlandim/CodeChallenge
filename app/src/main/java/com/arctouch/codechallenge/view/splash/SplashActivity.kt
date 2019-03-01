package com.arctouch.codechallenge.view.splash

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.view.base.BaseActivity
import com.arctouch.codechallenge.view.home.HomeActivity
import com.arctouch.codechallenge.viewmodel.GenreViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SplashActivity : BaseActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)


        val viewModel = ViewModelProviders.of(this).get(GenreViewModel::class.java)
        val disposable = viewModel.getGenres()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("TESTE", "1")
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
