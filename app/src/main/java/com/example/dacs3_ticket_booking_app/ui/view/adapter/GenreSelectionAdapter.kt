package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.example.dacs3_ticket_booking_app.databinding.ItemGenreSelectionBinding

class GenreSelectionAdapter(
    private val items: List<Genre>,
    private val onSelectionChanged: (Genre, Boolean) -> Unit
) : RecyclerView.Adapter<GenreSelectionAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

    inner class ViewHolder(private val binding: ItemGenreSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(genre: Genre) {
            binding.tvGenreName.text = genre.name

            binding.cbSelectGenre.isChecked = selectedItems.contains(genre.id)

            binding.cbSelectGenre.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(genre.id)
                } else {
                    selectedItems.remove(genre.id)
                }
                onSelectionChanged(genre, isChecked)
            }

            binding.root.setOnClickListener {
                binding.cbSelectGenre.isChecked = !binding.cbSelectGenre.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGenreSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    fun getSelectedGenres(): List<Genre> {
        return items.filter { selectedItems.contains(it.id) }
    }

    fun setSelectedGenres(genres: List<Genre>) {
        selectedItems.clear()
        selectedItems.addAll(genres.map { it.id })
        notifyDataSetChanged()
    }
}

