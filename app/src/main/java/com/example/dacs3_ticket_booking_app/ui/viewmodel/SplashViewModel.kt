package com.example.dacs3_ticket_booking_app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    private val _navigationEvent = MutableLiveData<String>()
    val navigationEvent: LiveData<String> = _navigationEvent

    // ✅ Chỉ dựa vào SharedPreferences (source of truth)
    fun checkUserAndNavigate(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userId = sharedPreferences.getString("userId", "")
        val role = sharedPreferences.getString("role", "")
        
        // DEBUG LOG
        Log.d("SplashViewModel", "=== CHECK USER ===")
        Log.d("SplashViewModel", "isLoggedIn: $isLoggedIn")
        Log.d("SplashViewModel", "userId: '$userId'")
        Log.d("SplashViewModel", "role: '$role' (trim: '${role?.trim()}')")
        
        // ✅ Kiểm tra SharedPreferences (không dùng FirebaseAuth!)
        val destination = if (isLoggedIn && !userId.isNullOrEmpty() && !role.isNullOrEmpty()) {
            val trimmedRole = role.trim().lowercase()
            Log.d("SplashViewModel", "trimmedRole: '$trimmedRole'")
            
            if (trimmedRole == "admin") {
                Log.d("SplashViewModel", "Navigate to: admin")
                "admin"
            } else {
                Log.d("SplashViewModel", "Navigate to: user")
                "user"
            }
        } else {
            Log.d("SplashViewModel", "Navigate to: login (not logged in)")
            "login"
        }
        
        _navigationEvent.value = destination
    }
}



