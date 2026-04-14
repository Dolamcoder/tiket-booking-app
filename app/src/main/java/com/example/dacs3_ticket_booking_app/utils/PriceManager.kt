package com.example.dacs3_ticket_booking_app.utils

/**
 * Quản lý giá vé theo khung giờ chiếu
 * sáng: trước 12h (100k)
 * chiều: 12h-18h (120k)
 * tối: sau 18h (150k)
 */
object PriceManager {
    // Giá vé theo priceTier (đơn vị: VND)
    private val prices = mapOf(
        "morning" to 100000.0,      // Sáng
        "afternoon" to 120000.0,    // Chiều
        "evening" to 150000.0       // Tối
    )

    /**
     * Lấy giá vé theo priceTier
     */
    fun getPrice(priceTier: String): Double {
        return prices[priceTier] ?: 100000.0  // Mặc định 100k
    }

    /**
     * Lấy label giá vé
     */
    fun getPriceLabel(priceTier: String): String {
        val price = getPrice(priceTier)
        return String.format("%,.0f đ", price)
    }

    /**
     * Tính tổng giá cho nhiều ghế
     */
    fun calculateTotal(priceTier: String, seatCount: Int): Double {
        return getPrice(priceTier) * seatCount
    }

    /**
     * Format giá thành chuỗi dễ đọc
     */
    fun formatPrice(price: Double): String {
        return String.format("%,.0f đ", price)
    }
}

