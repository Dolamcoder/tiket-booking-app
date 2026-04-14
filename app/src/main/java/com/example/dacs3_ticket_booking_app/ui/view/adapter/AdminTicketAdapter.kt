package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Ticket
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminTicketBinding
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class AdminTicketAdapter(
    private val items: MutableList<Ticket>,
    private val onCancel: (Ticket) -> Unit
) : RecyclerView.Adapter<AdminTicketAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemAdminTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: Ticket) {
            binding.tvTicketId.text = "Vé #${ticket.id.take(8)}"
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
                    .setTitle("Hủy vé")
                    .setMessage("Bạn có chắc muốn hủy vé #${ticket.id.take(8)} không?")
                    .setPositiveButton("Hủy vé") { _, _ -> onCancel(ticket) }
                    .setNegativeButton("Không", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
