package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemTimeSlotBinding
import com.example.dacs3_ticket_booking_app.utils.TimeSlotManager

class TimeSlotAdapter(
    private val slots: List<String>,
    private val onSlotSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedSlot: String? = null

    inner class TimeSlotViewHolder(private val binding: ItemTimeSlotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: String) {
            val slotLabel = TimeSlotManager.getSlotLabel(slot)
            binding.tvSlotTime.text = slotLabel
            binding.tvSlotStatus.text = "Còn trống"

            // Highlight nếu được chọn
            if (slot == selectedSlot) {
                binding.slotCardView.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_blue_light)
                )
                binding.tvSlotTime.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                binding.slotCardView.setCardBackgroundColor(
                    binding.root.context.getColor(com.example.dacs3_ticket_booking_app.R.color.admin_surface)
                )
                binding.tvSlotTime.setTextColor(
                    binding.root.context.getColor(com.example.dacs3_ticket_booking_app.R.color.admin_text_primary)
                )
            }

            binding.root.setOnClickListener {
                selectedSlot = slot
                notifyDataSetChanged()
                onSlotSelected(slot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(slots[position])
    }

    override fun getItemCount(): Int = slots.size

    fun setSelectedSlot(slot: String?) {
        selectedSlot = slot
        notifyDataSetChanged()
    }
}

