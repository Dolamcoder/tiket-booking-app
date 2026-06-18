package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemRevenueBinding

data class RevenueAdapterItem(
    val title: String,
    val totalRevenue: Double,
    val ticketCount: Int,
    val percentage: Int,
    val color: Int
)

class RevenueAdapter(
    private val items: List<RevenueAdapterItem>
) : RecyclerView.Adapter<RevenueAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRevenueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RevenueAdapterItem) {
            binding.tvMovieTitle.text = item.title
            
            val formatter = java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN"))
            binding.tvRevenue.text = formatter.format(item.totalRevenue.toLong()) + "₫"
            binding.tvTicketCount.text = "${item.ticketCount} vé (${item.percentage}%)"
            
            // Set circle indicator color
            binding.viewColorIndicator.background.setTint(item.color)
            
            // Set progress bar progress and tint color
            binding.progressBarShare.progress = item.percentage
            binding.progressBarShare.progressTintList = android.content.res.ColorStateList.valueOf(item.color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRevenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}

