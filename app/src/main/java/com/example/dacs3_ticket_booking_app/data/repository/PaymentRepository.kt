package com.example.dacs3_ticket_booking_app.data.repository

import android.util.Log
import com.example.dacs3_ticket_booking_app.data.api.PaymentRequest
import com.example.dacs3_ticket_booking_app.data.api.RetrofitClient

class PaymentRepository {
    private val paymentService = RetrofitClient.paymentService

    suspend fun initiatePayment(amount: Long): Result<String> {
        return try {
            Log.d("PaymentRepository", "Initiating payment with amount: $amount")
            val request = PaymentRequest(amount = amount)
            var payUrl = paymentService.payment(request)  // Server returns String directly
            
            // Làm sạch URL: xóa dấu ngoặc kép và khoảng trắng dư thừa
            payUrl = payUrl.trim().removeSurrounding("\"")
            
            // Sửa lỗi nếu thiếu dấu : sau https (ví dụ https// thành https://)
            if (payUrl.startsWith("https//")) {
                payUrl = payUrl.replaceFirst("https//", "https://")
            }
            
            Log.d("PaymentRepository", "Payment URL received and cleaned: $payUrl")
            Result.success(payUrl)
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Payment error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkPaymentStatus(resultCode: String): Result<Boolean> {
        return try {
            Log.d("PaymentRepository", " Checking payment status with code: $resultCode")
            val response = paymentService.checkPayment(resultCode)
            Log.d("PaymentRepository", "Payment check response: $response")
            // resultCode "0" = successful
            val isSuccess = resultCode == "0"
            Result.success(isSuccess)
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Check payment error: ${e.message}")
            Result.failure(e)
        }
    }
}


