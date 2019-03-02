package com.arctouch.codechallenge.view.details

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arctouch.codechallenge.databinding.MovieDetailsFragmentBinding
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.viewmodel.MovieDetailsViewModel
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.movie_details_fragment.*

class MovieDetailsFragment : Fragment() {

    private lateinit var movie: Movie
    var onFinishAnimationListener: YoYo.AnimatorCallback? = null

    companion object {
        fun newInstance(movie: Movie): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("movie", movie)
                }
            }

        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        movie = arguments?.getSerializable("movie") as Movie
        val viewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel::class.java)
        viewModel.movie.value = movie

        val binding = MovieDetailsFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overlay.visibility = View.INVISIBLE
        content.visibility = View.INVISIBLE
        btnClose.setOnClickListener { startCloseAnimation() }
        overlay.setOnClickListener { startCloseAnimation() }
        startInitialAnimation()
    }

    private fun startInitialAnimation() {
        YoYo.with(Techniques.FadeIn).onEnd {
            overlay.visibility = View.VISIBLE
            YoYo.with(Techniques.SlideInUp).onStart { content.visibility = View.VISIBLE }.duration(200).playOn(content)
        }.duration(200).playOn(overlay)
    }

    fun startCloseAnimation() {
        YoYo.with(Techniques.SlideOutDown).onEnd {
            content.visibility = View.INVISIBLE
            YoYo.with(Techniques.FadeOut).onEnd { animator ->
                overlay.visibility = View.INVISIBLE
                onFinishAnimationListener?.call(animator)
            }.duration(200).playOn(overlay)
        }.duration(200).playOn(content)
    }

}