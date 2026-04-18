package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ShowtimeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val showtimeCollection = db.collection("showtimes")

    // ✅ Thêm suất chiếu (kiểm tra trùng theo phòng, ngày, giờ)
    suspend fun addShowtime(showtime: Showtime): Result<String> {
        return try {
            // Kiểm tra xem suất chiếu đã tồn tại không
            val existing = showtimeCollection
                .whereEqualTo("roomId", showtime.roomId)
                .whereEqualTo("screeningDate", showtime.screeningDate)
                .whereEqualTo("timeSlot", showtime.timeSlot)
                .get()
                .await()
            
            if (!existing.isEmpty) {
                throw Exception("Suất chiếu ngày ${showtime.screeningDate} lúc ${showtime.timeSlot} đã tồn tại trong phòng này")
            }
            
            val docRef = showtimeCollection.document()
            val showtimeWithId = showtime.copy(id = docRef.id)
            docRef.set(showtimeWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy tất cả suất chiếu
    suspend fun getAllShowtimes(): Result<List<Showtime>> {
        return try {
            val snapshot = showtimeCollection.get().await()
            val list = snapshot.toObjects(Showtime::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy suất chiếu theo movieId
    suspend fun getShowtimesByMovie(movieId: String): Result<List<Showtime>> {
        return try {
            val snapshot = showtimeCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            val list = snapshot.toObjects(Showtime::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy suất chiếu theo roomId
    suspend fun getShowtimesByRoom(roomId: String): Result<List<Showtime>> {
        return try {
            val snapshot = showtimeCollection
                .whereEqualTo("roomId", roomId)
                .get()
                .await()
            val list = snapshot.toObjects(Showtime::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✨ NEW: Lấy suất chiếu theo roomId & ngày chiếu (dùng để tìm khung giờ trống)
    suspend fun getShowtimesByRoomAndDate(roomId: String, screeningDate: String): Result<List<Showtime>> {
        return try {
            val snapshot = showtimeCollection
                .whereEqualTo("roomId", roomId)
                .whereEqualTo("screeningDate", screeningDate)
                .get()
                .await()
            val list = snapshot.toObjects(Showtime::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cập nhật suất chiếu
    suspend fun updateShowtime(showtime: Showtime): Result<Unit> {
        return try {
            showtimeCollection.document(showtime.id).set(showtime).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa suất chiếu
    suspend fun deleteShowtime(showtimeId: String): Result<Unit> {
        return try {
            showtimeCollection.document(showtimeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔒 Lock ghế (Transaction an toàn, chống đặt trùng)
    // LOCK_TIMEOUT_MS = 2 phút (auto unlock nếu quá 2p)
    suspend fun lockSeats(showtimeId: String, positions: List<String>): Result<Unit> {
        val LOCK_TIMEOUT_MS = 2 * 60 * 1000L  // ✅ 2 phút
        val docRef = showtimeCollection.document(showtimeId)
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val showtime = snapshot.toObject(Showtime::class.java)
                    ?: throw Exception("Không tìm thấy suất chiếu")

                val now = System.currentTimeMillis()
                val booked = showtime.bookedSeats
                val locked = showtime.lockedSeats.toMutableMap()

                // ✅ Xóa các lock đã hết hạn (>2 phút)
                locked.entries.removeIf { (_, ts) -> now - ts > LOCK_TIMEOUT_MS }

                // ✅ Kiểm tra xem ghế có available không
                for (pos in positions) {
                    when {
                        booked.contains(pos) -> throw Exception("Ghế $pos đã được đặt rồi")
                        locked.containsKey(pos) -> throw Exception("Ghế $pos đang được giữ bởi người khác")
                    }
                }

                // ✅ Lock các ghế (thêm vào map)
                for (pos in positions) {
                    locked[pos] = now
                }
                
                transaction.update(docRef, "lockedSeats", locked)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ShowtimeRepository", "❌ Lock seats failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ Xác nhận ghế sau thanh toán thành công (Transaction)
    // Chuyển từ locked → booked
    suspend fun confirmBooking(showtimeId: String, positions: List<String>): Result<Unit> {
        val docRef = showtimeCollection.document(showtimeId)
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val showtime = snapshot.toObject(Showtime::class.java)
                    ?: throw Exception("Không tìm thấy suất chiếu")

                val booked = showtime.bookedSeats.toMutableList()
                val locked = showtime.lockedSeats.toMutableMap()

                // ✅ Chuyển từ locked → booked
                for (pos in positions) {
                    if (locked.containsKey(pos)) {
                        if (!booked.contains(pos)) {
                            booked.add(pos)
                        }
                        locked.remove(pos)
                        android.util.Log.d("ShowtimeRepository", "✅ Confirmed seat: $pos")
                    }
                }

                transaction.update(docRef, "bookedSeats", booked)
                transaction.update(docRef, "lockedSeats", locked)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ShowtimeRepository", "❌ Confirm booking failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ Mở khóa ghế khi user hủy / hết giờ thanh toán
    suspend fun releaseLockedSeats(showtimeId: String, positions: List<String>): Result<Unit> {
        val docRef = showtimeCollection.document(showtimeId)
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val showtime = snapshot.toObject(Showtime::class.java)
                    ?: throw Exception("Không tìm thấy suất chiếu")

                val locked = showtime.lockedSeats.toMutableMap()
                
                // ✅ Xóa lock cho từng ghế
                for (pos in positions) {
                    if (locked.containsKey(pos)) {
                        locked.remove(pos)
                        android.util.Log.d("ShowtimeRepository", "✅ Released lock for seat: $pos")
                    }
                }

                transaction.update(docRef, "lockedSeats", locked)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ShowtimeRepository", "❌ Release locked seats failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 🔍 Tìm kiếm suất chiếu theo movie name + date range + time range (Client-side)
    suspend fun searchShowtimes(
        movieId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        allShowtimes: List<Showtime>
    ): Result<List<Showtime>> {
        return try {
            var filtered = allShowtimes

            // Lọc theo movieId
            if (movieId != null && movieId.isNotEmpty()) {
                filtered = filtered.filter { it.movieId == movieId }
            }

            // Lọc theo ngày chiếu (format: "dd/MM/yyyy")
            if (startDate != null && endDate != null) {
                filtered = filtered.filter { it.screeningDate >= startDate && it.screeningDate <= endDate }
            } else if (startDate != null) {
                filtered = filtered.filter { it.screeningDate >= startDate }
            } else if (endDate != null) {
                filtered = filtered.filter { it.screeningDate <= endDate }
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 📊 Sắp xếp suất chiếu theo ngày chiếu và khung giờ
    fun sortShowtimesByDate(showtimes: List<Showtime>, ascending: Boolean = true): List<Showtime> {
        return if (ascending) {
            showtimes.sortedWith(compareBy<Showtime> { it.screeningDate }.thenBy { it.timeSlot })
        } else {
            showtimes.sortedWith(compareBy<Showtime> { it.screeningDate }.thenBy { it.timeSlot }.reversed())
        }
    }

    // 📊 Sắp xếp suất chiếu theo khung giá
    fun sortShowtimesByPriceTier(showtimes: List<Showtime>): List<Showtime> {
        val tierOrder = mapOf("morning" to 1, "afternoon" to 2, "evening" to 3)
        return showtimes.sortedBy { tierOrder[it.priceTier] ?: 0 }
    }

    // ✅ Thêm 1 ghế vào danh sách booked
    suspend fun addBookedSeat(showtimeId: String, seatPosition: String): Result<Unit> {
        return try {
            val docRef = showtimeCollection.document(showtimeId)
            val showtime = docRef.get().await().toObject(Showtime::class.java)
                ?: throw Exception("Không tìm thấy suất chiếu")

            val bookedSeats = showtime.bookedSeats.toMutableList()
            
            if (!bookedSeats.contains(seatPosition)) {
                bookedSeats.add(seatPosition)
                docRef.update("bookedSeats", bookedSeats).await()
                android.util.Log.d("ShowtimeRepository", "✅ Added booked seat: $seatPosition")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ShowtimeRepository", "❌ Add booked seat failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ Xóa 1 ghế khỏi danh sách booked (khi hủy hóa đơn)
    suspend fun removeBookedSeat(showtimeId: String, seatPosition: String): Result<Unit> {
        return try {
            val docRef = showtimeCollection.document(showtimeId)
            val showtime = docRef.get().await().toObject(Showtime::class.java)
                ?: throw Exception("Không tìm thấy suất chiếu")

            val bookedSeats = showtime.bookedSeats.toMutableList()
            
            if (bookedSeats.contains(seatPosition)) {
                bookedSeats.remove(seatPosition)
                docRef.update("bookedSeats", bookedSeats).await()
                android.util.Log.d("ShowtimeRepository", "✅ Removed booked seat: $seatPosition")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ShowtimeRepository", "❌ Remove booked seat failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 📅 Lấy danh sách ngày chiếu duy nhất theo movieId
    suspend fun getScreeningDatesByMovie(movieId: String): Result<List<String>> {
        return try {
            val snapshot = showtimeCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            val showtimes = snapshot.toObjects(Showtime::class.java)
            // Lấy ngày duy nhất và sắp xếp
            val uniqueDates = showtimes.map { it.screeningDate }.distinct().sorted()
            Result.success(uniqueDates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ⏰ Lấy danh sách khung giờ theo movieId và ngày chiếu
    suspend fun getTimeSlotsByMovieAndDate(movieId: String, screeningDate: String): Result<List<String>> {
        return try {
            val snapshot = showtimeCollection
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("screeningDate", screeningDate)
                .get()
                .await()
            val showtimes = snapshot.toObjects(Showtime::class.java)
            // Lấy khung giờ duy nhất (đã sắp xếp)
            val timeSlots = showtimes.map { it.timeSlot }.distinct()
            Result.success(timeSlots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy chi tiết suất chiếu theo ID
    suspend fun getShowtimeById(showtimeId: String): Result<Showtime?> {
        return try {
            val doc = showtimeCollection.document(showtimeId).get().await()
            val showtime = doc.toObject(Showtime::class.java)
            Result.success(showtime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
