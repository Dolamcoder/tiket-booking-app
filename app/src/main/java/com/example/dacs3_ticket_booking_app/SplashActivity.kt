package com.example.dacs3_ticket_booking_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.databinding.ActivitySplashBinding
import com.example.dacs3_ticket_booking_app.ui.view.MainActivity
import com.example.dacs3_ticket_booking_app.ui.view.admin.AdminActivity
import com.example.dacs3_ticket_booking_app.ui.view.auth.LoginActivity
import com.example.dacs3_ticket_booking_app.ui.viewmodel.SplashViewModel

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        
        splashViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        
        // ✅ Observe navigation event
        splashViewModel.navigationEvent.observe(this) { destination ->
            when (destination) {
                "admin" -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                }
                "user" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                "login" -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
            finish()
        }
        
        // ✅ Check user and navigate
        binding.startBtn.setOnClickListener {
            splashViewModel.checkUserAndNavigate(this)
        }
    }
}