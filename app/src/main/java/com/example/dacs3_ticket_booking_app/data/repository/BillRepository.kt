package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillRepository {

    private val db = FirebaseFirestore.getInstance()
    private val billCollection = db.collection("bills")

    // ✅ Add new bill
    suspend fun addBill(bill: Bill): Result<String> {
        return try {
            val docRef = billCollection.document()
            val billWithId = bill.copy(id = docRef.id)
            docRef.set(billWithId).await()
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

    // ✅ Get bills by userId
    suspend fun getBillsByUser(userId: String): Result<List<Bill>> {
        return try {
            val snapshot = billCollection
                .whereEqualTo("userId", userId)
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

    // ✅ Update bill status (e.g. paid -> cancelled)
    suspend fun updateBillStatus(billId: String, status: String): Result<Unit> {
        return try {
            billCollection.document(billId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Delete bill
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
}
