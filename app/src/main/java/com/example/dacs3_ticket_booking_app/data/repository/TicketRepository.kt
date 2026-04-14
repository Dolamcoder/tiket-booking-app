package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Ticket
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TicketRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ticketCollection = db.collection("tickets")

    // ✅ Thêm vé mới
    suspend fun addTicket(ticket: Ticket): Result<String> {
        return try {
            val docRef = ticketCollection.document()
            val ticketWithId = ticket.copy(id = docRef.id)
            docRef.set(ticketWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy tất cả vé (Admin)
    suspend fun getAllTickets(): Result<List<Ticket>> {
        return try {
            val snapshot = ticketCollection.get().await()
            val list = snapshot.toObjects(Ticket::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy vé theo userId
    suspend fun getTicketsByUser(userId: String): Result<List<Ticket>> {
        return try {
            val snapshot = ticketCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val list = snapshot.toObjects(Ticket::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy vé theo showtimeId
    suspend fun getTicketsByShowtime(showtimeId: String): Result<List<Ticket>> {
        return try {
            val snapshot = ticketCollection
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .await()
            val list = snapshot.toObjects(Ticket::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cập nhật trạng thái vé (VD: paid -> cancelled)
    suspend fun updateTicketStatus(ticketId: String, status: String): Result<Unit> {
        return try {
            ticketCollection.document(ticketId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa vé
    suspend fun deleteTicket(ticketId: String): Result<Unit> {
        return try {
            ticketCollection.document(ticketId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🎬 Lấy vé theo movieId (thông qua showtimeId)
    suspend fun getTicketsByMovie(movieId: String): Result<List<Ticket>> {
        return try {
            val snapshot = ticketCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            val list = snapshot.toObjects(Ticket::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
