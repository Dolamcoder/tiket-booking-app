package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminMovieBinding

class AdminMovieAdapter(
    private val items: MutableList<Movie>,
    private val onEdit: (Movie) -> Unit,
    private val onDetail: (Movie) -> Unit,
    private val onDelete: (Movie) -> Unit
) : RecyclerView.Adapter<AdminMovieAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvStatus.text = when (movie.status) {
                "now_showing" -> "Đang chiếu"
                "coming_soon" -> "Sắp chiếu"
                "ended" -> "Đã kết thúc"
                else -> movie.status
            }
            binding.tvDuration.text = "${movie.duration} phút"
            Glide.with(binding.root.context)
                .load(movie.poster)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.imgPoster)

            binding.root.setOnClickListener { onDetail(movie) }
            binding.btnEdit.setOnClickListener { onEdit(movie) }
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Xóa phim")
                    .setMessage("Bạn có chắc muốn xóa \"${movie.title}\" không?")
                    .setPositiveButton("Xóa") { _, _ -> onDelete(movie) }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
