package com.example.dacs3_ticket_booking_app.data.model

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val poster: String = "",
    val trailer: String = "",
    val duration: Int = 0,
    val status: String = "coming_soon", // now_showing | coming_soon | ended
    val year: Int = 0,
    val releaseDate: String = "",
    val genres: List<String> = emptyList(),
    val casts: List<Cast> = emptyList(),
)
