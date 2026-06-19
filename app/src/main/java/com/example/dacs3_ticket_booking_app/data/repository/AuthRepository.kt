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
                
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Lỗi tạo user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Pair<String, String>> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Lỗi đăng nhập"))

            user.reload().await()

            val docRef = userCollection.document(user.uid)
            val doc = docRef.get().await()

            if (!doc.exists()) {

                if (!user.isEmailVerified) {
                    return Result.failure(Exception("Vui lòng xác thực email"))
                }

                val newUser = User(
                    id = user.uid,
                    email = email,
                    fullName = user.displayName ?: "",
                    role = "user",
                    isActivate = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                docRef.set(newUser).await()

                return Result.success(Pair(user.uid, "user"))
            }

            val userData = doc.toObject(User::class.java)

            if (userData == null || !userData.isActivate) {
                return Result.failure(Exception("Tài khoản bị khóa"))
            }

            val role = userData.role ?: "user"

            Result.success(Pair(user.uid, role))

        } catch (e: Exception) {
            Result.failure(Exception("Sai email hoặc mật khẩu"))
        }
    }
    suspend fun getUserFromFirestore(userId: String): Result<User?> {
        return try {
            val doc = userCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logout() {
        firebaseAuth.signOut()
    }

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




