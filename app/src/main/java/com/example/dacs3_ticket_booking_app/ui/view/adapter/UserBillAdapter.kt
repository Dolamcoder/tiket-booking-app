package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.example.dacs3_ticket_booking_app.databinding.ItemUserBillBinding
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class UserBillAdapter(
    private val items: MutableList<Bill>
) : RecyclerView.Adapter<UserBillAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemUserBillBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bill: Bill) {
            binding.tvBillId.text = "Bill #${bill.id.take(8)}"
            binding.tvBookingTime.text = "Booked: ${sdf.format(Date(bill.bookingTime))}"
            
            // Display list of seats
            val seatsDisplay = bill.seatPositions.joinToString(", ") { SeatUtils.positionToDisplay(it) }
            val seatCount = bill.seatPositions.size
            binding.tvSeats.text = "Seats: $seatsDisplay ($seatCount ghế)"
            
            // ✅ Tính tổng tiền = price (giá đơn vị) * số lượng ghế
            val totalPrice = bill.price * seatCount
            binding.tvTotalPrice.text = "Total: ${totalPrice.toInt()}đ"
            
            binding.tvStatus.text = when (bill.status) {
                "paid" -> "Paid"
                "cancelled" -> "Cancelled"
                else -> bill.status
            }
            
            // Color based on status
            binding.tvStatus.setTextColor(
                when (bill.status) {
                    "paid" -> binding.root.context.resources.getColor(android.R.color.holo_green_light, null)
                    "cancelled" -> binding.root.context.resources.getColor(android.R.color.holo_red_light, null)
                    else -> binding.root.context.resources.getColor(android.R.color.white, null)
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}



