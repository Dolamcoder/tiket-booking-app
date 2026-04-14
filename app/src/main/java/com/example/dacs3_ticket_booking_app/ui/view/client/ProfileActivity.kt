package com.example.dacs3_ticket_booking_app.ui.view.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3_ticket_booking_app.SplashActivity
import com.example.dacs3_ticket_booking_app.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        firebaseAuth = FirebaseAuth.getInstance()

        // ✅ Hiển thị thông tin user
        displayUserInfo()

        // ✅ Nút Logout
        binding.logoutBtn.setOnClickListener {
            logout()
        }

        // ✅ Nút Quay lại
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun displayUserInfo() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // Hiển thị thông tin từ Firebase Auth
            binding.userNameText.text = currentUser.displayName ?: "Chưa cập nhật tên"
            binding.userEmailText.text = currentUser.email ?: "Không có email"
        }
    }

    private fun logout() {
        // ✅ Đăng xuất Firebase
        firebaseAuth.signOut()

        // ✅ Xóa session từ SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            remove("userId")
            remove("role")
            putBoolean("isLoggedIn", false)
            apply()
        }

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

        // ✅ Quay lại Splash Screen
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}