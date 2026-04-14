package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    // ✅ Đăng ký (Register)
    suspend fun register(email: String, fullName: String, password: String): Result<String> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            if (user != null) {
                // Cập nhật displayName
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                
                user.updateProfile(profileUpdate).await()
                
                // Lưu thông tin vào Firestore
                val userData = User(
                    id = user.uid,
                    email = email,
                    fullName = fullName,
                    role = "user",
                    accumulatedMoney = 0.0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                userCollection.document(user.uid).set(userData).await()
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Lỗi tạo user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Đăng nhập (Login)
    suspend fun login(email: String, password: String): Result<Pair<String, String>> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            if (user != null) {
                // Lấy thông tin user từ Firestore để có role
                val doc = userCollection.document(user.uid).get().await()
                val userData = doc.toObject(User::class.java)
                val role = userData?.role ?: "user"
                
                Result.success(Pair(user.uid, role))
            } else {
                Result.failure(Exception("Lỗi đăng nhập"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy thông tin user từ Firestore
    suspend fun getUserFromFirestore(userId: String): Result<User?> {
        return try {
            val doc = userCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy user hiện tại
    fun getCurrentUser(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // ✅ Kiểm tra user đã đăng nhập
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // ✅ Đăng xuất
    fun logout() {
        firebaseAuth.signOut()
    }

    // ✅ Cập nhật tiền tích lũy
    suspend fun updateAccumulatedMoney(userId: String, amount: Double): Result<Unit> {
        return try {
            val doc = userCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            
            if (user != null) {
                val newAmount = user.accumulatedMoney + amount
                userCollection.document(userId).update(
                    "accumulatedMoney", newAmount,
                    "updatedAt", System.currentTimeMillis()
                ).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Không tìm thấy user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




