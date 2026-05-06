package com.example.dacs3_ticket_booking_app.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class GenerateQRRequest(
    val billId: String,
    val endTime: Long  // milliseconds
)

data class GenerateQRResponse(
    val success: Boolean = false,
    val qrData: QRData? = null,
    val qrImage: String? = null,  // Base64 image data
    val error: String? = null
)

data class QRData(
    val billId: String = "",
    val endTime: Long = 0L,
    val signature: String = ""
)

data class VerifyQRRequest(
    val billId: String = "",
    val endTime: Long = 0L,
    val signature: String = ""
)

data class VerifyQRResponse(
    val valid: Boolean = false,
    val message: String = "",
    val error: String? = null
)

interface QRService {  
    @POST("/api/generate-qr")
    suspend fun generateQR(@Body request: GenerateQRRequest): Response<GenerateQRResponse>

    @POST("/api/verify-qr")
    suspend fun verifyQR(@Body request: VerifyQRRequest): Response<VerifyQRResponse>
}