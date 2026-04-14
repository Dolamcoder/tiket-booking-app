package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.dacs3_ticket_booking_app.R
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemTimeBinding

class TimeAdapter(
    private val timeSlots: List<String>,
    private val onTimeSelected: (String) -> Unit = {}
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class TimeViewHolder(private val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(time: String, position: Int) {
            binding.textViewTime.text = time
            if (selectedPosition == position) {
                binding.textViewTime.setBackgroundResource(R.drawable.yellow_bg)
                binding.textViewTime.setTextColor(binding.root.context.getColor(R.color.black))
            } else {
                binding.textViewTime.setBackgroundResource(R.drawable.light_black_bg)
                binding.textViewTime.setTextColor(binding.root.context.getColor(R.color.white))
            }
            binding.root.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    lastSelectedPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(lastSelectedPosition)
                    notifyItemChanged(selectedPosition)
                    onTimeSelected(time)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        return TimeViewHolder(
            ItemTimeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(timeSlots[position], position)
    }

    override fun getItemCount(): Int = timeSlots.size
}

