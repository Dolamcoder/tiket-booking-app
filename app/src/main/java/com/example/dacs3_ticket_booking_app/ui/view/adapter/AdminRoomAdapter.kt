package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminRoomBinding

class AdminRoomAdapter(
    private val items: MutableList<Room>,
    private val onEdit: (Room) -> Unit,
    private val onDelete: (Room) -> Unit,
    private val onDetail: (Room) -> Unit = {}
) : RecyclerView.Adapter<AdminRoomAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.tvRoomName.text = room.name
            binding.tvCapacity.text = "${room.rowCount} hàng x ${room.colCount} cột"
            val totalSeats = room.seatLayout.sumOf { row -> row.count { it == '1' } }
            binding.tvTotalSeats.text = "Tổng: $totalSeats ghế"

            binding.btnDetail.setOnClickListener { onDetail(room) }
            binding.btnEdit.setOnClickListener { onEdit(room) }
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Xóa phòng chiếu")
                    .setMessage("Bạn có chắc muốn xóa \"${room.name}\" không?")
                    .setPositiveButton("Xóa") { _, _ -> onDelete(room) }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
