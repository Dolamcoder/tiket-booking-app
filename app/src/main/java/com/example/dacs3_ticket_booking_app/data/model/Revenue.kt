package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Revenue(
    var id: String = "",
    var movieId: String = "",
    var movieTitle: String = "",
    var showtimeId: String = "",
    var ticketCount: Int = 0,
    var totalRevenue: Double = 0.0,
    var date: Long = 0L, // epoch millis
    var createdAt: Long = System.currentTimeMillis()
) : Serializable

