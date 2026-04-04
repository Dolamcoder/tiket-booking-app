package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Cast
import com.example.dacs3_ticket_booking_app.databinding.ItemCastBinding

class CastAdapter(private val casts: List<Cast>) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(private val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cast: Cast) {
            binding.tvCastName.text = cast.name
            
            if (cast.images.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(cast.images)
                    .placeholder(R.drawable.back_dark)
                    .error(R.drawable.back_dark)
                    .circleCrop()
                    .into(binding.ivCastImage)
            } else {
                binding.ivCastImage.setImageResource(R.drawable.back_dark)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(casts[position])
    }

    override fun getItemCount(): Int = casts.size
}

