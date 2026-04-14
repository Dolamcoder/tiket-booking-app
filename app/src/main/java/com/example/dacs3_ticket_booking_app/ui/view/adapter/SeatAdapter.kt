package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.databinding.ItemSeatBinding
import com.example.dacs3_ticket_booking_app.utils.SeatUtils

// Adapter sử dụng List<String> (vị trí) thay String trực tiếp
class SeatAdapter(
    private val seatPositions: List<String>,
    private val selectedSeat: SelectedSeat,
    private val seatNameMap: Map<String, String> = emptyMap()  // Lưu position -> name từ SeatCell
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {
    private val selectedPositions = mutableSetOf<String>()
    
    // Trạng thái ghế: AVAILABLE, SELECTED, UNAVAILABLE
    private val seatStatus = mutableMapOf<String, SeatStatus>()

    enum class SeatStatus {
        AVAILABLE, SELECTED, UNAVAILABLE
    }

    inner class SeatViewHolder(private val binding: ItemSeatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: String) {
            val status = seatStatus[position] ?: SeatStatus.AVAILABLE
            // Lấy tên ghế từ map (A1-A8, hoặc trống nếu '0')
            val seatName = seatNameMap[position] ?: ""
            
            binding.seatTxt.text = seatName
            
            // Kiểm tra nếu là vị trí trống (seatName rỗng)
            val isEmpty = seatName.isEmpty()
            
            if (isEmpty) {
                // Vị trí trống - không hiển thị gì
                binding.root.isEnabled = false
                binding.seatTxt.text = ""
                binding.seatTxt.setBackgroundColor(Color.TRANSPARENT)
                return
            }
            
            // Ghế ngồi - hiển thị bình thường
            when (status) {
                SeatStatus.AVAILABLE -> {
                    binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_available)
                    binding.seatTxt.setTextColor(binding.root.context.getColor(R.color.white))
                }
                SeatStatus.SELECTED -> {
                    binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_selected)
                    binding.seatTxt.setTextColor(binding.root.context.getColor(R.color.black))
                }
                SeatStatus.UNAVAILABLE -> {
                    binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_unavailable)
                    binding.seatTxt.setTextColor(binding.root.context.getColor(R.color.grey))
                }
            }
            
            binding.root.setOnClickListener {
                if (isEmpty) return@setOnClickListener
                
                val currentStatus = seatStatus[position] ?: SeatStatus.AVAILABLE
                if (currentStatus == SeatStatus.AVAILABLE) {
                    seatStatus[position] = SeatStatus.SELECTED
                    selectedPositions.add(position)
                } else if (currentStatus == SeatStatus.SELECTED) {
                    seatStatus[position] = SeatStatus.AVAILABLE
                    selectedPositions.remove(position)
                }
                notifyItemChanged(adapterPosition)
                // Hiển thị tên ghế (bỏ vị trí trống)
                val selected = selectedPositions.map { seatNameMap[it] ?: "" }.filter { it.isNotEmpty() }.joinToString(", ")
                selectedSeat.Return(selected, selectedPositions.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        return SeatViewHolder(
            ItemSeatBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        holder.bind(seatPositions[position])
    }

    override fun getItemCount(): Int = seatPositions.size

    interface SelectedSeat {
        fun Return(selectedName: String, num: Int)
    }
}

