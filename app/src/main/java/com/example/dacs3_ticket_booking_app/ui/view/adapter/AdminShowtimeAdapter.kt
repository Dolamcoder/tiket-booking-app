package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminShowtimeBinding
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class AdminShowtimeAdapter(
    private val items: MutableList<Showtime>,
    private val onEdit: (Showtime) -> Unit,
    private val onDelete: (Showtime) -> Unit,
    private val onDetail: (Showtime) -> Unit = {},
    // Maps to resolve names from IDs
    private val movieTitleMap: Map<String, String> = emptyMap(),
    private val roomNameMap: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<AdminShowtimeAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemAdminShowtimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(showtime: Showtime) {
            val movieTitle = movieTitleMap[showtime.movieId] ?: "ID: ${showtime.movieId.take(8)}..."
            val roomName = roomNameMap[showtime.roomId] ?: "ID: ${showtime.roomId.take(8)}..."

            binding.tvMovieName.text = movieTitle
            binding.tvRoomName.text = roomName
            binding.tvStartTime.text = "Ngày: ${showtime.screeningDate}"
            binding.tvEndTime.text = "Giờ: ${showtime.timeSlot}"
            binding.tvPriceTier.text = SeatUtils.priceTierLabel(showtime.priceTier)
            binding.tvBookedSeats.text = "Đã đặt: ${showtime.bookedSeats.size}"

            binding.btnDetail.setOnClickListener { onDetail(showtime) }
            binding.btnEdit.setOnClickListener { onEdit(showtime) }
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Xóa suất chiếu")
                    .setMessage("Bạn có chắc muốn xóa suất chiếu này không?")
                    .setPositiveButton("Xóa") { _, _ -> onDelete(showtime) }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminShowtimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
