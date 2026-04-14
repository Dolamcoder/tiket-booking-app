package com.example.dacs3_ticket_booking_app.utils

import com.example.dacs3_ticket_booking_app.data.model.Showtime
import java.text.SimpleDateFormat
import java.util.*

object TimeSlotManager {
    // Định nghĩa các slot cố định mỗi ngày (2 tiếng mỗi slot, cách 30p)
    private val TIME_SLOTS = listOf(
        "08:00-10:00",
        "10:30-12:30",
        "13:00-15:00",
        "15:30-17:30",
        "18:00-20:00",
        "20:30-22:30"
    )

    /**
     * Lấy danh sách khung giờ khả dụng (chưa có suất nào) cho một phòng trong một ngày
     * Loại bỏ những khung giờ đã qua nếu là ngày hôm nay
     *
     * @param date Ngày chiếu (format: "dd/MM/yyyy")
     * @param roomId ID của phòng chiếu
     * @param existingShowtimes Danh sách các suất đã tồn tại trong ngày + phòng này
     * @return Danh sách các slot chưa được đặt và chưa qua thời gian
     */
    fun getAvailableSlots(
        date: String,
        roomId: String,
        existingShowtimes: List<Showtime>
    ): List<String> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = System.currentTimeMillis()
        val todayStr = sdf.format(Date(now))
        
        // Lọc chỉ các showtime của phòng này
        val roomShowtimes = existingShowtimes.filter { it.roomId == roomId }

        // Tạo set các slot đã được chiếm
        val bookedSlots = mutableSetOf<String>()
        roomShowtimes.forEach { showtime ->
            if (showtime.timeSlot.isNotEmpty()) {
                bookedSlots.add(showtime.timeSlot)
            }
        }

        // Lọc các slot khả dụng
        var availableSlots = TIME_SLOTS.filter { it !in bookedSlots }
        
        // Nếu là ngày hôm nay, loại bỏ các slot đã qua
        if (date == todayStr) {
            availableSlots = availableSlots.filter { slot ->
                val (slotStartTime, _) = calculateStartEndTime(date, slot)
                slotStartTime > now
            }
        }
        // Nếu là ngày quá khứ, không cho chọn slot nào
        else {
            val dateObj = sdf.parse(date)
            val dateTime = dateObj?.time ?: Long.MAX_VALUE
            val endOfDay = dateTime + 24 * 60 * 60 * 1000 - 1
            if (endOfDay < now) {
                availableSlots = emptyList()
            }
        }

        return availableSlots
    }

    /**
     * Tính toán startTime và endTime (Unix milliseconds) từ ngày + slot
     *
     * @param date Ngày chiếu (format: "dd/MM/yyyy")
     * @param timeSlot Khung giờ (format: "HH:mm-HH:mm", e.g. "10:30-12:30")
     * @return Pair(startTimeMillis, endTimeMillis)
     */
    fun calculateStartEndTime(
        date: String,
        timeSlot: String
    ): Pair<Long, Long> {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val parts = timeSlot.split("-")
        
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid timeSlot format: $timeSlot")
        }

        val startTimeStr = "$date ${parts[0].trim()}"
        val endTimeStr = "$date ${parts[1].trim()}"

        val startTime = sdf.parse(startTimeStr)?.time ?: 0L
        val endTime = sdf.parse(endTimeStr)?.time ?: 0L

        return Pair(startTime, endTime)
    }

    /**
     * Lấy label khung giờ cho hiển thị (e.g. "10:30-12:30" -> "10:30 - 12:30")
     */
    fun getSlotLabel(timeSlot: String): String {
        val parts = timeSlot.split("-")
        return if (parts.size == 2) {
            "${parts[0].trim()} - ${parts[1].trim()}"
        } else {
            timeSlot
        }
    }

    /**
     * Lấy danh sách tất cả các slot trong ngày
     */
    fun getAllSlots(): List<String> = TIME_SLOTS

    /**
     * Kiểm tra xem slot có hợp lệ không
     */
    fun isValidSlot(timeSlot: String): Boolean = timeSlot in TIME_SLOTS

    /**
     * Lấy priceTier dựa vào timeSlot
     */
    fun getPriceTierFromSlot(timeSlot: String): String {
        val startHour = timeSlot.substring(0, 2).toIntOrNull() ?: return "morning"
        return when (startHour) {
            in 8..11 -> "morning"      // 8:00 - 11:59
            in 12..17 -> "afternoon"   // 12:00 - 17:59
            else -> "evening"          // 18:00+
        }
    }
}

