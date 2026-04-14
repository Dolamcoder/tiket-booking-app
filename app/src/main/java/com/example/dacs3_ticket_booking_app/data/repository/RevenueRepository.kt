package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Revenue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RevenueRepository {

    private val db = FirebaseFirestore.getInstance()
    private val revenueCollection = db.collection("revenues")

    // ✅ Lấy tất cả doanh thu
    suspend fun getAllRevenues(): Result<List<Revenue>> {
        return try {
            val snapshot = revenueCollection.get().await()
            val revenues = snapshot.toObjects(Revenue::class.java)
            Result.success(revenues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy doanh thu theo movie
    suspend fun getRevenueByMovie(movieId: String): Result<List<Revenue>> {
        return try {
            val snapshot = revenueCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            val revenues = snapshot.toObjects(Revenue::class.java)
            Result.success(revenues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy doanh thu theo khoảng ngày
    suspend fun getRevenueByDateRange(startDate: Long, endDate: Long): Result<List<Revenue>> {
        return try {
            val snapshot = revenueCollection
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()
            val revenues = snapshot.toObjects(Revenue::class.java)
            Result.success(revenues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Tính tổng doanh thu
    fun calculateTotalRevenue(revenues: List<Revenue>): Double {
        return revenues.sumOf { it.totalRevenue }
    }

    // ✅ Tính tổng vé bán
    fun calculateTotalTickets(revenues: List<Revenue>): Int {
        return revenues.sumOf { it.ticketCount }
    }

    // ✅ Doanh thu theo phim (Local processing)
    fun groupRevenueByMovie(revenues: List<Revenue>): Map<String, Double> {
        return revenues.groupingBy { it.movieTitle }
            .fold(0.0) { acc, revenue -> acc + revenue.totalRevenue }
    }

    // ✅ Doanh thu theo ngày (Local processing)
    fun groupRevenueByDate(revenues: List<Revenue>): Map<Long, Double> {
        return revenues.groupingBy { it.date }
            .fold(0.0) { acc, revenue -> acc + revenue.totalRevenue }
    }

    // ✅ Thêm bản ghi doanh thu mới
    suspend fun addRevenue(revenue: Revenue): Result<String> {
        return try {
            val docRef = revenueCollection.document()
            val revenueWithId = revenue.copy(id = docRef.id)
            docRef.set(revenueWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa bản ghi doanh thu
    suspend fun deleteRevenue(revenueId: String): Result<Unit> {
        return try {
            revenueCollection.document(revenueId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy doanh thu theo showtimeId
    suspend fun getRevenueByShowtime(showtimeId: String): Result<List<Revenue>> {
        return try {
            val snapshot = revenueCollection
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .await()
            val revenues = snapshot.toObjects(Revenue::class.java)
            Result.success(revenues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
