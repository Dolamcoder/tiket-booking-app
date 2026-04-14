package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Showtime(
    var id: String = "",
    var movieId: String = "",
    var roomId: String = "",
    var screeningDate: String = "",  // NEW: "dd/MM/yyyy"
    var timeSlot: String = "",       // NEW: "08:00-10:00", "10:30-12:30", etc
    // "morning" = before 12:00, "afternoon" = 12:00-18:00, "evening" = after 18:00
    var priceTier: String = "morning",
    // Booked seat positions stored as "row_col", e.g. ["0_1", "0_2"]
    var bookedSeats: List<String> = emptyList(),
    // Seats locked during checkout: position -> lock timestamp (epoch millis)
    var lockedSeats: Map<String, Long> = emptyMap()
) : Serializable
