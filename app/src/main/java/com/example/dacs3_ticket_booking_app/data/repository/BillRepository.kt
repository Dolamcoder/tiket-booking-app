package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.example.dacs3_ticket_booking_app.data.model.Revenue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillRepository {

    private val db = FirebaseFirestore.getInstance()
    private val billCollection = db.collection("bills")
    private val revenueCollection = db.collection("revenues")

    // ✅ Add new bill
    suspend fun addBill(bill: Bill): Result<String> {
        return try {
            val docRef = billCollection.document()
            val billWithId = bill.copy(id = docRef.id)
            docRef.set(billWithId).await()
            
            // 💰 Tạo revenue record khi bill được tạo (nếu status là "paid")
            if (bill.status == "paid") {
                try {
                    val showtimeDoc = db.collection("showtimes").document(bill.showtimeId).get().await()
                    val movieId = showtimeDoc.getString("movieId") ?: ""
                    
                    val movieDoc = db.collection("movies").document(movieId).get().await()
                    val movieTitle = movieDoc.getString("title") ?: "Unknown"
                    
                    val revenue = Revenue(
                        id = "",
                        movieId = movieId,
                        movieTitle = movieTitle,
                        showtimeId = bill.showtimeId,
                        ticketCount = bill.seatPositions.size,
                        totalRevenue = bill.price * bill.seatPositions.size,
                        date = bill.bookingTime,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    val revenueDocRef = revenueCollection.document()
                    revenueCollection.document(revenueDocRef.id).set(revenue.copy(id = revenueDocRef.id)).await()
                } catch (e: Exception) {
                    // Ignore revenue creation error, bill was already saved
                }
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Get all bills (Admin)
    suspend fun getAllBills(): Result<List<Bill>> {
        return try {
            val snapshot = billCollection.get().await()
            val list = snapshot.toObjects(Bill::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBillsByUser(userId: String): Result<List<Bill>> {
        return try {
            val snapshot = billCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "paid")
                .get()
                .await()
            val list = snapshot.toObjects(Bill::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Get bills by showtimeId
    suspend fun getBillsByShowtime(showtimeId: String): Result<List<Bill>> {
        return try {
            val snapshot = billCollection
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .await()
            val list = snapshot.toObjects(Bill::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBillStatus(billId: String, status: String): Result<Unit> {
        return try {
            billCollection.document(billId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(billId: String): Result<Unit> {
        return try {
            billCollection.document(billId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🎬 Get bills by movieId (through showtimeId)
    suspend fun getBillsByMovie(movieId: String): Result<List<Bill>> {
        return try {
            val snapshot = billCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            val list = snapshot.toObjects(Bill::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Get bill by ID
    suspend fun getBillById(billId: String): Result<Bill?> {
        return try {
            val snapshot = billCollection.document(billId).get().await()
            val bill = snapshot.toObject(Bill::class.java)
            Result.success(bill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateBillQRData(billId: String, qrCodeData: String): Result<Unit> {
        return try {
            billCollection.document(billId).update(
                mapOf(
                    "qrCodeData" to qrCodeData,
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
