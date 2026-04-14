package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    // ✅ Lấy tất cả users
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = userCollection.get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy user theo ID
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val doc = userCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa user
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cập nhật role user
    suspend fun updateUserRole(userId: String, newRole: String): Result<Unit> {
        return try {
            userCollection.document(userId).update(
                "role", newRole,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cập nhật thông tin user
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy users theo role
    suspend fun getUsersByRole(role: String): Result<List<User>> {
        return try {
            val snapshot = userCollection.whereEqualTo("role", role).get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

