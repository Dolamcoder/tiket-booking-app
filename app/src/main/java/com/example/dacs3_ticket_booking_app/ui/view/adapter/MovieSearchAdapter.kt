package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ItemMovieSearchBinding

class MovieSearchAdapter(
    private val movies: MutableList<Movie> = mutableListOf(),
    private val onMovieSelected: (Movie) -> Unit
) : RecyclerView.Adapter<MovieSearchAdapter.MovieViewHolder>() {

    private var selectedMovie: Movie? = null
    private val allMovies = mutableListOf<Movie>()

    init {
        // Khởi tạo allMovies khi adapter được tạo
        allMovies.addAll(movies)
    }

    inner class MovieViewHolder(private val binding: ItemMovieSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieYear.text = movie.year.toString()

            // Load poster image using Glide
            if (movie.poster.isNotEmpty()) {
                Glide.with(binding.root)
                    .load(movie.poster)
                    .into(binding.ivMoviePoster)
            }

            // Show check icon if selected
            binding.ivCheck.visibility = if (movie.id == selectedMovie?.id) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            binding.root.setOnClickListener {
                selectedMovie = movie
                notifyDataSetChanged()
                onMovieSelected(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun setMovies(newMovies: List<Movie>) {
        allMovies.clear()
        allMovies.addAll(newMovies)
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun filterByQuery(query: String) {
        movies.clear()
        if (query.isEmpty()) {
            movies.addAll(allMovies)
        } else {
            movies.addAll(
                allMovies.filter { movie ->
                    movie.title.contains(query, ignoreCase = true)
                }
            )
        }
        notifyDataSetChanged()
    }

    fun setSelectedMovie(movie: Movie?) {
        selectedMovie = movie
        notifyDataSetChanged()
    }
}

