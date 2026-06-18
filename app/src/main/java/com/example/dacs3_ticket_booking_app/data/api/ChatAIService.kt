package com.example.dacs3_ticket_booking_app.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

data class ChatAIRequest(
    val message : String,
)
data class ChatAIResponse(
    val reply : String,
)

interface ChatService {
    @POST("/api/chat")
    suspend fun getChatResponse(@Body request: ChatAIRequest): Response<ChatAIResponse>
}


