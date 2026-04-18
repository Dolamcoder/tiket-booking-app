package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RoomRepository {

    private val db = FirebaseFirestore.getInstance()
    private val roomCollection = db.collection("rooms")

    // ✅ Thêm phòng chiếu
    suspend fun addRoom(room: Room): Result<String> {
        return try {
            val docRef = roomCollection.document()
            val roomWithId = room.copy(id = docRef.id)
            docRef.set(roomWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy tất cả phòng
    suspend fun getAllRooms(): Result<List<Room>> {
        return try {
            val snapshot = roomCollection.get().await()
            val rooms = snapshot.toObjects(Room::class.java)
            Result.success(rooms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy phòng theo ID
    suspend fun getRoomById(id: String): Result<Room?> {
        return try {
            val doc = roomCollection.document(id).get().await()
            val room = doc.toObject(Room::class.java)
            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cập nhật phòng
    suspend fun updateRoom(room: Room): Result<Unit> {
        return try {
            roomCollection.document(room.id).set(room).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa phòng
    suspend fun deleteRoom(roomId: String): Result<Unit> {
        return try {
            roomCollection.document(roomId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy phòng theo showtimeId
    suspend fun getRoomByShowtimeId(showtimeId: String): Result<Room?> {
        return try {
            val showtimeCollection = db.collection("showtimes")
            val showtimeDoc = showtimeCollection.document(showtimeId).get().await()
            val roomId = showtimeDoc.getString("roomId")

            if (roomId != null) {
                val room = roomCollection.document(roomId).get().await().toObject(Room::class.java)
                Result.success(room)
            } else {
                Result.failure(Exception("Room ID not found in showtime"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
