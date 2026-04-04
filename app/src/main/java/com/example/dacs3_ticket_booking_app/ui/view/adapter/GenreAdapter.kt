package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemGenreBinding

class GenreAdapter(private val genres: List<String>) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    inner class GenreViewHolder(private val binding: ItemGenreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(genre: String) {
            binding.tvGenre.text = genre
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(genres[position])
    }

    override fun getItemCount(): Int = genres.size
}

