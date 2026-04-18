package com.example.dacs3_ticket_booking_app.data.model

import java.io.Serializable

data class User(
    var id: String = "",
    var email: String = "",
    var fullName: String = "",
    var role: String = "user", // "user" | "admin"
    var accumulatedMoney: Double = 0.0, // Tien tich luy (so tien da mua ve)
    var avatar: String = "", // Avatar URL from Cloudinary
    var createdAt: Long = 0L,
    var isActivate:Boolean=false,
    var updatedAt: Long = 0L
) : Serializable
