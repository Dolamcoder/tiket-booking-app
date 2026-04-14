package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Bill(
    var id: String = "",
    var userId: String = "",
    var showtimeId: String = "",
    var seatPositions: List<String> = emptyList(),  // Danh sách ghế, ví dụ ["0_1", "0_2", "0_3"]
    var price: Double = 0.0,                         // Giá vé đơn vị tại thời điểm đặt
    var bookingTime: Long = 0L,
    var status: String = "paid",                     // "paid" | "cancelled"
    var qrCodeData: String = ""
) : Serializable
