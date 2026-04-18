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

    // ✅ Đăng ký (Register) - Chỉ tạo Auth, chưa tạo Firestore (chống hacker)
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
                
                // Gửi email xác thực
                user.sendEmailVerification().await()
                
                // ⚠️ KHÔNG lưu Firestore ngay - chỉ lưu khi user xác thực email bằng login
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
                // ⭐ BƯỚC 1: Lấy thông tin user từ Firestore trước
                val doc = userCollection.document(user.uid).get().await()
                val userData = doc.toObject(User::class.java)
                
                // ⭐ BƯỚC 2: Nếu Firestore null → User tự đăng ký, chưa xác thực email
                if (userData == null) {
                    if (!user.isEmailVerified) {
                        user.sendEmailVerification().await()
                        return Result.failure(Exception("Vui lòng xác thực email trước khi đăng nhập"))
                    }
                    
                    // Email đã xác thực → Tạo user mới vào Firestore
                    val newUser = User(
                        id = user.uid,
                        email = email,
                        fullName = user.displayName ?: "",
                        role = "user",
                        isActivate = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    userCollection.document(user.uid).set(newUser).await()
                    return Result.success(Pair(user.uid, "user"))
                }
                
                // ⭐ BƯỚC 3: User tồn tại trong Firestore (Admin tạo hoặc từ trước)
                // Kiểm tra tài khoản bị khóa
                if (!userData.isActivate) {
                    return Result.failure(Exception("Tài khoản của bạn đã bị khóa"))
                }
                
                val role = userData.role ?: "user"
                Result.success(Pair(user.uid, role))
            } else {
                Result.failure(Exception("Lỗi đăng nhập"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sai email hoặc mật khẩu"))
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

    // ...existing code...

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




