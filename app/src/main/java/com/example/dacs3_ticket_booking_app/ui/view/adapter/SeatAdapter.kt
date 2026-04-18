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
    // ✅ Sử dụng Set để tự động tránh trùng lặp
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
                
                // ✅ Chỉ cho phép chọn/bỏ chọn ghế AVAILABLE hoặc SELECTED
                // Ghế UNAVAILABLE không thể click
                if (currentStatus == SeatStatus.UNAVAILABLE) {
                    return@setOnClickListener
                }
                
                if (currentStatus == SeatStatus.AVAILABLE) {
                    seatStatus[position] = SeatStatus.SELECTED
                    selectedPositions.add(position)  // Set tự động tránh trùng lặp
                } else if (currentStatus == SeatStatus.SELECTED) {
                    seatStatus[position] = SeatStatus.AVAILABLE
                    selectedPositions.remove(position)
                }
                
                notifyItemChanged(adapterPosition)
                
                // ✅ Gửi danh sách ghế được chọn (tên ghế, không vị trí)
                val selected = selectedPositions
                    .mapNotNull { seatNameMap[it]?.takeIf { name -> name.isNotEmpty() } }
                    .sorted()  // Sắp xếp theo tên ghế
                    .joinToString(", ")
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

    // ✅ Lấy danh sách tên ghế đã chọn (sắp xếp)
    fun getSelectedSeatNames(): List<String> {
        return selectedPositions
            .mapNotNull { seatNameMap[it]?.takeIf { name -> name.isNotEmpty() } }
            .sorted()
    }

    // ✅ Lấy danh sách vị trí ghế đã chọn (position)
    fun getSelectedPositions(): List<String> {
        return selectedPositions.toList()
    }

    // ✅ Reset tất cả ghế về AVAILABLE
    fun resetSelection() {
        selectedPositions.forEach { position ->
            seatStatus[position] = SeatStatus.AVAILABLE
        }
        selectedPositions.clear()
        notifyDataSetChanged()
    }

    // ✅ Update trạng thái ghế từ server (real-time listener)
    fun updateSeatsStatus(unavailablePositions: List<String>) {
        // ✅ Reset tất cả ghế về AVAILABLE trước
        seatStatus.clear()
        
        // ✅ Restore lại các ghế đã select (SELECTED)
        selectedPositions.forEach { pos ->
            seatStatus[pos] = SeatStatus.SELECTED
        }
        
        // ✅ Đặt ghế unavailable (locked/booked) - nhưng không override SELECTED
        unavailablePositions.forEach { position ->
            // ✅ Không set UNAVAILABLE nếu ghế đang SELECTED (của chính user)
            if (!selectedPositions.contains(position)) {
                seatStatus[position] = SeatStatus.UNAVAILABLE
            }
        }
        notifyDataSetChanged()
    }

    // ✅ Đặt trạng thái ghế (dùng khi tải danh sách ghế đã được đặt)
    fun setUnavailableSeats(unavailablePositions: List<String>) {
        // ✅ Reset tất cả ghế về AVAILABLE trước
        seatStatus.clear()
        
        // ✅ Restore lại các ghế đã select
        selectedPositions.forEach { pos ->
            seatStatus[pos] = SeatStatus.SELECTED
        }
        
        // ✅ Đặt ghế unavailable (locked/booked) - nhưng không override SELECTED
        unavailablePositions.forEach { position ->
            // ✅ Không set UNAVAILABLE nếu ghế đang SELECTED
            if (!selectedPositions.contains(position)) {
                seatStatus[position] = SeatStatus.UNAVAILABLE
            }
        }
        notifyDataSetChanged()
    }

    interface SelectedSeat {
        fun Return(selectedName: String, num: Int)
    }
}

