package com.example.dacs3_ticket_booking_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3_ticket_booking_app.databinding.ActivitySplashBinding
import com.example.dacs3_ticket_booking_app.ui.view.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize mock database
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}