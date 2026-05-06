package com.example.dacs3_ticket_booking_app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Use ServerConfig to switch between Emulator and Physical Device
    private val baseUrl: String
        get() = ServerConfig.BASE_URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val paymentService: PaymentService by lazy {
        retrofit.create(PaymentService::class.java)
    }
    val qrService: QRService by lazy {
        retrofit.create(QRService::class.java)
    }
}




