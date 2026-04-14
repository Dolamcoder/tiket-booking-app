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
    private val onCancel: (Bill) -> Unit
) : RecyclerView.Adapter<AdminBillAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemAdminBillBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: Bill) {
            binding.tvBillId.text = "Bill #${ticket.id.take(8)}"
            binding.tvBookingTime.text = "Đặt lúc: ${sdf.format(Date(ticket.bookingTime))}"
            binding.tvSeats.text = "Ghế: ${SeatUtils.positionToDisplay(ticket.seatPosition)}"
            binding.tvTotalPrice.text = "Giá: ${ticket.price}đ"
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
