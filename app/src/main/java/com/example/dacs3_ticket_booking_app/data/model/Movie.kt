package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Movie(
   var id: String = "",
    var title: String = "",
    var description: String = "",
    var poster: String = "",
    var trailer: String = "",
    var duration: Int = 0,
    var status: String = "coming_soon", // now_showing | coming_soon | ended
    var year: Int = 0,
    var releaseDate: String = "",
    var genres: List<String> = emptyList(),
    var casts: List<Cast> = emptyList(),
    var ticketsSold: Int = 0,           // Số vé đã bán
    var totalRevenue: Double = 0.0,     // Tổng doanh thu
) : Serializable
