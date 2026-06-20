package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class Review(
    var id: String = "",
    var movieId: String = "",
    var userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var rating: Float = 0f,
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
) : Serializable
