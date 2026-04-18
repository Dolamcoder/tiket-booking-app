package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminBillBinding
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class AdminBillAdapter(
    private val items: MutableList<Bill>,
    private val onCancel: (Bill) -> Unit,
    private val onItemClick: ((Bill) -> Unit)? = null
) : RecyclerView.Adapter<AdminBillAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemAdminBillBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: Bill) {
            binding.tvBillId.text = "Bill #${ticket.id.take(8)}"
            binding.tvBookingTime.text = "Đặt lúc: ${sdf.format(Date(ticket.bookingTime))}"
            // Hiển thị danh sách ghế
            val seatsDisplay = ticket.seatPositions.joinToString(", ") { SeatUtils.positionToDisplay(it) }
            val seatCount = ticket.seatPositions.size
            binding.tvSeats.text = "Ghế: $seatsDisplay ($seatCount ghế)"
            // ✅ Tính tổng tiền = price (giá đơn vị) * số lượng ghế
            val totalPrice = ticket.price * seatCount
            binding.tvTotalPrice.text = "Tổng: ${totalPrice.toInt()}đ"
            binding.tvStatus.text = when (ticket.status) {
                "paid" -> "Đã thanh toán"
                "cancelled" -> "Đã hủy"
                else -> ticket.status
            }

            binding.btnCancel.isEnabled = ticket.status == "paid"
            binding.btnCancel.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Cancel Bill")
                    .setMessage("Are you sure you want to cancel bill #${ticket.id.take(8)}?")
                    .setPositiveButton("Cancel Bill") { _, _ -> onCancel(ticket) }
                    .setNegativeButton("No", null)
                    .show()
            }

            // ✅ Add click listener to view detail
            binding.root.setOnClickListener {
                onItemClick?.invoke(ticket)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
