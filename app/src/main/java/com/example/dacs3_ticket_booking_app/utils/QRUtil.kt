package com.example.dacs3_ticket_booking_app.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object QRUtils {
    /**
     * Tính endTime từ screeningDate và timeSlot
     *
     * @param screeningDate: String (format: "dd/MM/yyyy", e.g., "15/05/2026")
     * @param timeSlot: String (format: "HH:mm-HH:mm", e.g., "08:00-10:00")
     * @return Long (milliseconds since epoch)
     */
    fun calculateEndTime(screeningDate: String, timeSlot: String): Long {
        return try {
            // Parse screeningDate
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val screenDate = dateFormat.parse(screeningDate)

            // Extract endTime from timeSlot (e.g., "08:00-10:00" -> "10:00")
            val times = timeSlot.split("-")
            if (times.size < 2) {
                Log.e("QRUtils", "❌ Invalid timeSlot format: $timeSlot")
                return System.currentTimeMillis()
            }

            val endTimeStr = times[1].trim()  // "10:00"

            // Combine screenDate + endTime
            val calendar = Calendar.getInstance()
            calendar.time = screenDate

            // Parse hour and minute
            val timeParts = endTimeStr.split(":")
            if (timeParts.size >= 2) {
                val hour = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1].toIntOrNull() ?: 0

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                val endTime = calendar.timeInMillis
                Log.d("QRUtils", "✅ EndTime calculated: screenDate=$screeningDate, timeSlot=$timeSlot -> endTime=$endTime")
                return endTime
            } else {
                Log.e("QRUtils", "❌ Invalid time format: $endTimeStr")
                return System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e("QRUtils", "❌ Exception: ${e.message}")
            return System.currentTimeMillis()
        }
    }
}
