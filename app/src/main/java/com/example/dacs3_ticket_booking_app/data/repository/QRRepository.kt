package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.api.GenerateQRRequest
import com.example.dacs3_ticket_booking_app.data.api.GenerateQRResponse
import com.example.dacs3_ticket_booking_app.data.api.RetrofitClient
import com.example.dacs3_ticket_booking_app.data.api.VerifyQRRequest
import com.example.dacs3_ticket_booking_app.data.api.VerifyQRResponse
import android.util.Log

class QRRepository {
    private val qrService = RetrofitClient.qrService

    suspend fun generateQR(billId: String, endTime: Long): Result<GenerateQRResponse> = try {
        val request = GenerateQRRequest(billId, endTime)
        val response = qrService.generateQR(request)

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success) {
                Result.success(body)
            } else {
                Result.failure(Exception(body.error ?: "Unknown error"))
            }
        } else {
            Result.failure(Exception("API error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun verifyQR(billId: String, endTime: Long, signature: String): Result<VerifyQRResponse> = try {
        Log.d("QRRepository", "🔄 Verifying QR for bill: $billId")
        val request = VerifyQRRequest(billId, endTime, signature)
        val response = qrService.verifyQR(request)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            Log.d("QRRepository", "✅ QR verification result: valid=${body.valid}, message=${body.message}")
            Result.success(body)
        } else {
            Log.e("QRRepository", "❌ Verify API error: ${response.code()}")
            Result.failure(Exception("API error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e("QRRepository", "❌ Exception: ${e.message}")
        Result.failure(e)
    }
}