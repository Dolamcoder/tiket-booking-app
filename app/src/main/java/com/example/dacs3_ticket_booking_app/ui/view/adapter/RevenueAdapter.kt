package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemRevenueBinding

class RevenueAdapter(
    private val items: List<Pair<String, Double>>
) : RecyclerView.Adapter<RevenueAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRevenueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<String, Double>) {
            binding.tvMovieTitle.text = item.first
            binding.tvRevenue.text = String.format("%.0f", item.second) + "₫"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRevenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}

