package com.example.dacs3_ticket_booking_app.ui.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.databinding.ActivityRegisterBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.registerBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val fullName = binding.fullNameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (validateInput(email, fullName, password, confirmPassword)) {
                authViewModel.register(email, fullName, password)
            }
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        observeViewModel()
    }

    private fun validateInput(email: String, fullName: String, password: String, confirmPassword: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.emailInput.error = "Email không được để trống"
                false
            }
            fullName.isEmpty() -> {
                binding.fullNameInput.error = "Họ tên không được để trống"
                false
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "Mật khẩu không được để trống"
                false
            }
            confirmPassword.isEmpty() -> {
                binding.confirmPasswordInput.error = "Vui lòng xác nhận mật khẩu"
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
            password != confirmPassword -> {
                binding.confirmPasswordInput.error = "Mật khẩu không trùng khớp"
                false
            }
            fullName.length < 3 -> {
                binding.fullNameInput.error = "Họ tên phải có ít nhất 3 ký tự"
                false
            }
            else -> true
        }
    }

    private fun observeViewModel() {
        authViewModel.successMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearSuccessMessage()
                
                startActivity(Intent(this, LoginActivity::class.java))
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
            binding.registerBtn.isEnabled = !isLoading
        }
    }
}



