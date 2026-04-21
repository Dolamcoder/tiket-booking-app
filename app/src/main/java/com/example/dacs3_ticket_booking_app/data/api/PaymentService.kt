package com.example.dacs3_ticket_booking_app.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

data class PaymentRequest(
    val amount: Long
)

// Server trả về String (payUrl) trực tiếp, không phải JSON object
// Nên không cần PaymentResponse data class nữa

interface PaymentService {
    @POST("/api/payment")
    suspend fun payment(@Body request: PaymentRequest): String  // Server trả về payUrl string

    @POST("/api/check-payment")
    suspend fun checkPayment(@Query("resultCode") resultCode: String): String
}


