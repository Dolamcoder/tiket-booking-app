package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Bill
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
            
            // Nếu revenues collection rỗng, tính từ bills
            if (revenues.isEmpty()) {
                return getRevenuesFromBills()
            }
            Result.success(revenues)
        } catch (e: Exception) {
            // Fallback to bills calculation
            return getRevenuesFromBills()
        }
    }
    
    // ✅ Tính doanh thu từ Bills (fallback method)
    private suspend fun getRevenuesFromBills(): Result<List<Revenue>> {
        return try {
            val billsCollection = db.collection("bills")
            val billsSnapshot = billsCollection
                .whereEqualTo("status", "paid")
                .get()
                .await()
            
            val bills = billsSnapshot.toObjects(Bill::class.java)
            
            // Group by movieId and create revenue records
            val showtimesCollection = db.collection("showtimes")
            val moviesCollection = db.collection("movies")
            
            val revenueMap = mutableMapOf<String, Revenue>()
            
            for (bill in bills) {
                try {
                    val showtimeDoc = showtimesCollection.document(bill.showtimeId).get().await()
                    val movieId = showtimeDoc.getString("movieId") ?: continue
                    
                    val movieDoc = moviesCollection.document(movieId).get().await()
                    val movieTitle = movieDoc.getString("title") ?: "Unknown"
                    
                    val key = "$movieId|$movieTitle"
                    val ticketCount = bill.seatPositions.size
                    val totalAmount = bill.price * ticketCount
                    
                    revenueMap[key] = if (revenueMap.containsKey(key)) {
                        val existing = revenueMap[key]!!
                        existing.copy(
                            ticketCount = existing.ticketCount + ticketCount,
                            totalRevenue = existing.totalRevenue + totalAmount
                        )
                    } else {
                        Revenue(
                            id = "",
                            movieId = movieId,
                            movieTitle = movieTitle,
                            showtimeId = bill.showtimeId,
                            ticketCount = ticketCount,
                            totalRevenue = totalAmount,
                            date = bill.bookingTime
                        )
                    }
                } catch (e: Exception) {
                    // Skip this bill if error
                }
            }
            
            Result.success(revenueMap.values.toList())
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
            
            // Nếu revenues collection rỗng, tính từ bills
            if (revenues.isEmpty()) {
                return getRevenuesFromBillsByDateRange(startDate, endDate)
            }
            Result.success(revenues)
        } catch (e: Exception) {
            return getRevenuesFromBillsByDateRange(startDate, endDate)
        }
    }
    
    // ✅ Tính doanh thu từ Bills trong khoảng ngày
    private suspend fun getRevenuesFromBillsByDateRange(startDate: Long, endDate: Long): Result<List<Revenue>> {
        return try {
            val billsCollection = db.collection("bills")
            val billsSnapshot = billsCollection.get().await()
            
            val bills = billsSnapshot.toObjects(Bill::class.java)
            
            // Filter by date range and status = "paid"
            val filteredBills = bills.filter { 
                it.status == "paid" && it.bookingTime >= startDate && it.bookingTime <= endDate
            }
            
            val showtimesCollection = db.collection("showtimes")
            val moviesCollection = db.collection("movies")
            
            val revenueMap = mutableMapOf<String, Revenue>()
            
            for (bill in filteredBills) {
                try {
                    val showtimeDoc = showtimesCollection.document(bill.showtimeId).get().await()
                    val movieId = showtimeDoc.getString("movieId") ?: continue
                    
                    val movieDoc = moviesCollection.document(movieId).get().await()
                    val movieTitle = movieDoc.getString("title") ?: "Unknown"
                    
                    val key = "$movieId|$movieTitle"
                    val ticketCount = bill.seatPositions.size
                    val totalAmount = bill.price * ticketCount
                    
                    revenueMap[key] = if (revenueMap.containsKey(key)) {
                        val existing = revenueMap[key]!!
                        existing.copy(
                            ticketCount = existing.ticketCount + ticketCount,
                            totalRevenue = existing.totalRevenue + totalAmount
                        )
                    } else {
                        Revenue(
                            id = "",
                            movieId = movieId,
                            movieTitle = movieTitle,
                            showtimeId = bill.showtimeId,
                            ticketCount = ticketCount,
                            totalRevenue = totalAmount,
                            date = bill.bookingTime
                        )
                    }
                } catch (e: Exception) {
                    // Skip this bill if error
                }
            }
            
            Result.success(revenueMap.values.toList())
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

    // ✅ Doanh thu theo phòng (Local processing) 
    fun groupRevenueByRoom(showtimeIds: List<String>): Map<String, Double> {
        // showtimeIds contain room info, need to fetch from database
        // For now, we'll implement a simpler version that groups by showtimeId
        return emptyMap()
    }
    
    // ✅ Lấy doanh thu theo phòng từ bills
    suspend fun getRevenueByRoomFromBills(): Result<Map<String, Double>> {
        return try {
            val billsCollection = db.collection("bills")
            val billsSnapshot = billsCollection
                .whereEqualTo("status", "paid")
                .get()
                .await()
            
            val bills = billsSnapshot.toObjects(Bill::class.java)
            
            val showtimesCollection = db.collection("showtimes")
            val roomsCollection = db.collection("rooms")
            
            val revenueByRoom = mutableMapOf<String, Double>()
            
            for (bill in bills) {
                try {
                    val showtimeDoc = showtimesCollection.document(bill.showtimeId).get().await()
                    val roomId = showtimeDoc.getString("roomId") ?: continue
                    
                    val roomDoc = roomsCollection.document(roomId).get().await()
                    val roomName = roomDoc.getString("name") ?: "Unknown"
                    
                    val amount = bill.price * bill.seatPositions.size
                    revenueByRoom[roomName] = (revenueByRoom[roomName] ?: 0.0) + amount
                } catch (e: Exception) {
                    // Skip
                }
            }
            
            Result.success(revenueByRoom)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ✅ Lấy doanh thu theo phòng trong khoảng ngày
    suspend fun getRevenueByRoomFromBillsByDateRange(startDate: Long, endDate: Long): Result<Map<String, Double>> {
        return try {
            val billsCollection = db.collection("bills")
            val billsSnapshot = billsCollection.get().await()
            
            val bills = billsSnapshot.toObjects(Bill::class.java)
            
            val filteredBills = bills.filter { 
                it.status == "paid" && it.bookingTime >= startDate && it.bookingTime <= endDate
            }
            
            val showtimesCollection = db.collection("showtimes")
            val roomsCollection = db.collection("rooms")
            
            val revenueByRoom = mutableMapOf<String, Double>()
            
            for (bill in filteredBills) {
                try {
                    val showtimeDoc = showtimesCollection.document(bill.showtimeId).get().await()
                    val roomId = showtimeDoc.getString("roomId") ?: continue
                    
                    val roomDoc = roomsCollection.document(roomId).get().await()
                    val roomName = roomDoc.getString("name") ?: "Unknown"
                    
                    val amount = bill.price * bill.seatPositions.size
                    revenueByRoom[roomName] = (revenueByRoom[roomName] ?: 0.0) + amount
                } catch (e: Exception) {
                    // Skip
                }
            }
            
            Result.success(revenueByRoom)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
