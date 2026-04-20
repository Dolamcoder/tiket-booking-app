package com.example.dacs3_ticket_booking_app.ui.view.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.databinding.ActivityLoginBinding
import com.example.dacs3_ticket_booking_app.ui.view.MainActivity
import com.example.dacs3_ticket_booking_app.ui.view.admin.AdminActivity
import com.example.dacs3_ticket_booking_app.ui.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // ✅ Nút Đăng nhập
        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        // ✅ Link đến trang đăng ký
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.emailInput.error = "Email không được để trống"
                false
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "Mật khẩu không được để trống"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailInput.error = "Email không hợp lệ"
                false
            }
            password.length < 6 -> {
                binding.passwordInput.error = "Mật khẩu phải có ít nhất 6 ký tự"
                false
            }
            else -> true
        }
    }

    private fun observeViewModel() {
        authViewModel.loginSuccess.observe(this) { (userId, role) ->
            if (userId != null && role != null) {
                // ✅ Lưu state vào SharedPreferences
                sharedPreferences.edit().apply {
                    putString("userId", userId)
                    putString("role", role)
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                // ✅ Điều hướng dựa trên role
                when (role) {
                    "admin" -> {
                        startActivity(Intent(this, AdminActivity::class.java))
                    }
                    else -> {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
                finish()
            }
        }

        authViewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearErrorMessage()
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loginBtn.isEnabled = !isLoading
        }
    }
}




