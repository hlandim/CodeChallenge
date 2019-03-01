package com.arctouch.codechallenge.view.details

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arctouch.codechallenge.R
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.movie_details_fragment.*

class MovieDetailsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        overlay.visibility = View.INVISIBLE
        content.visibility = View.INVISIBLE
        return inflater.inflate(R.layout.movie_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startInitialAnimation()
    }

    private fun startInitialAnimation() {
        YoYo.with(Techniques.FadeIn).onEnd {
            overlay.visibility = View.VISIBLE
            YoYo.with(Techniques.SlideInUp).onStart { content.visibility = View.VISIBLE }.duration(200).playOn(content)
        }.duration(200).playOn(overlay)
    }
}