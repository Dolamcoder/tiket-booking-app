package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Bill(
    var id: String = "",
    var userId: String = "",
    var showtimeId: String = "",
    var seatPosition: String = "",      // 1 ghế duy nhất, ví dụ "0_1"
    var price: Double = 0.0,            // Giá vé
    var bookingTime: Long = 0L,
    var status: String = "paid",        // "paid" | "cancelled"
    var qrCodeData: String = ""
) : Serializable
