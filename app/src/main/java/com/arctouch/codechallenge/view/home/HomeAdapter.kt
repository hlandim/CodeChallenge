package com.arctouch.codechallenge.view.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.MovieImageUrlBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.movie_item.view.*
import kotlinx.android.synthetic.main.row_loading.view.*


class HomeAdapter(private var movies: MutableList<Movie>) : RecyclerView.Adapter<HomeAdapter.CustomViewHolder>() {

    lateinit var listener: ListListener
    private var isLoading = false
    private var countNewItems = movies.size

    companion object {
        const val ITEM = 1
        const val LOADING = 2
    }

    init {
    }

    abstract class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(movie: Movie)
    }

    class ViewHolder(itemView: View) : CustomViewHolder(itemView) {
        override fun bind(movie: Movie) {
            itemView.titleTextView.text = movie.title
            itemView.genresTextView.text = movie.genres?.joinToString(separator = ", ") { it.name }
            itemView.releaseDateTextView.text = movie.releaseDate

            Glide.with(itemView)
                    .load(movie.posterPath?.let { MovieImageUrlBuilder.buildPosterUrl(it) })
                    .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                    .into(itemView.posterImageView)
        }
    }

    class FooterHolder(itemView: View) : CustomViewHolder(itemView) {
        override fun bind(movie: Movie) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

        return if (viewType == ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(com.arctouch.codechallenge.R.layout.movie_item, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(com.arctouch.codechallenge.R.layout.row_loading, parent, false)
            FooterHolder(view)
        }

    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        if (position < movies.size) {
            val movie = movies[position]
            holder.bind(movie)
            holder.itemView.setOnClickListener {
                listener.onRowClicked(movie)
            }
        } else {
            val footerHolder = holder as FooterHolder
            if (!isLoading && countNewItems > 0) {
                countNewItems = 0
                isLoading = true
                footerHolder.itemView.pbLoadingNewMovies.visibility = View.VISIBLE
            } else if (countNewItems <= 0) {
                footerHolder.itemView.pbLoadingNewMovies.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return movies.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == movies.size) LOADING else ITEM
    }

    interface ListListener {
        fun onRowClicked(movie: Movie)
        fun onBottomReached()

    }

    fun replaceItems(movies: MutableList<Movie>) {
        isLoading = false
        countNewItems = movies.size - this.movies.size
        this.movies = movies
        notifyDataSetChanged()
    }
}
