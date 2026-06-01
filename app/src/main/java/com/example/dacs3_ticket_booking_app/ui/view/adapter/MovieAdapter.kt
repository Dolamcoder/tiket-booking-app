package com.example.dacs3_ticket_booking_app.ui.view.adaper

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ViewholderFilmBinding
import com.example.dacs3_ticket_booking_app.ui.view.DetailMovieActivity

class MovieAdapter(
    private val items: MutableList<Movie>,
    private val onMovieClick: ((Movie) -> Unit)? = null
): RecyclerView.Adapter<MovieAdapter.ViewHolder>() {
    private var context: Context?=null
    inner class ViewHolder(private val binding: ViewholderFilmBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.nameTxt.text=movie.title
            val requestOption= RequestOptions()
                .transform(CenterCrop(), RoundedCorners(30))
            Glide.with(context!!)
                .load(movie.poster)
                .apply(requestOption)
                .into(binding.picture)
            
            // Add click listener to navigate to detail movie
            binding.root.setOnClickListener {
                if (onMovieClick != null) {
                    onMovieClick.invoke(movie)
                } else {
                    val intent = Intent(context, DetailMovieActivity::class.java)
                    intent.putExtra("MOVIE_ID", movie.id)
                    context?.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieAdapter.ViewHolder {
        context=parent.context
        val binding= ViewholderFilmBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount():Int=items.size

    // Update movies in the adapter
    fun updateMovies(newMovies: MutableList<Movie>) {
        items.clear()
        items.addAll(newMovies)
        notifyDataSetChanged()
    }
}