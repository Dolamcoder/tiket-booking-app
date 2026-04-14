package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Room(
    var id: String = "",
    var name: String = "",
    var rowCount: Int = 0,
    var colCount: Int = 0,
    // Each string = 1 row. '1' = seat, '0' = aisle/empty
    // e.g. ["1110111", "1110111"]
    var seatLayout: List<String> = emptyList()
) : Serializable
