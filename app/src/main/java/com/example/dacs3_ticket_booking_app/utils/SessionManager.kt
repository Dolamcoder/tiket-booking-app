package com.example.dacs3_ticket_booking_app.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.dacs3_ticket_booking_app.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_session",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_USER = "current_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // ✅ Lưu thông tin user khi đăng nhập
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString(KEY_USER, userJson)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // ✅ Lấy thông tin user hiện tại
    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Lấy ID user hiện tại
    fun getCurrentUserId(): String? {
        return getUser()?.id
    }

    // ✅ Kiểm tra user đã đăng nhập chưa
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // ✅ Đăng xuất
    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    // ✅ Cập nhật thông tin user hiện tại
    fun updateUser(user: User) {
        saveUser(user)
    }

    // ✅ Xóa toàn bộ session
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

