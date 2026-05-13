package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3_ticket_booking_app.SplashActivity
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminBinding
import com.example.dacs3_ticket_booking_app.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Init Cloudinary once
        CloudinaryHelper.init(this)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        setupNavigation()
        
        // ✅ Nút Logout
        binding.logoutBtn.setOnClickListener {
            logout()
        }
    }

    private fun setupNavigation() {
        binding.cardMovies.setOnClickListener {
            startActivity(Intent(this, AdminMovieListActivity::class.java))
        }
        binding.cardRooms.setOnClickListener {
            startActivity(Intent(this, AdminRoomListActivity::class.java))
        }
        binding.cardShowtimes.setOnClickListener {
            startActivity(Intent(this, AdminShowtimeListActivity::class.java))
        }
        binding.cardTickets.setOnClickListener {
            startActivity(Intent(this, AdminBillListActivity::class.java))
        }
        binding.cardUsers.setOnClickListener {
            startActivity(Intent(this, AdminUserListActivity::class.java))
        }
        binding.cardRevenue.setOnClickListener {
            startActivity(Intent(this, AdminRevenueActivity::class.java))
        }
        binding.cardQrCode.setOnClickListener {
            startActivity(Intent(this, AdminQRScanActivity::class.java))
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
