package com.example.dacs3_ticket_booking_app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SplashViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _navigationEvent = MutableLiveData<String>()
    val navigationEvent: LiveData<String> = _navigationEvent

    // ✅ Kiểm tra user và role từ SharedPreferences
    fun checkUserAndNavigate(context: Context) {
        val firebaseUser = firebaseAuth.currentUser
        
        if (firebaseUser != null) {
            // User đã đăng nhập Firebase, lấy role từ SharedPreferences
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val role = sharedPreferences.getString("role", "user") ?: "user"
            
            _navigationEvent.value = if (role == "admin") "admin" else "user"
        } else {
            // Chưa đăng nhập
            _navigationEvent.value = "login"
        }
    }
}



