package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginSuccess = MutableLiveData<Pair<String?, String?>>()
    val loginSuccess: LiveData<Pair<String?, String?>> = _loginSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // ✅ Đăng nhập
    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.onSuccess { (userId, role) ->
                _loginSuccess.value = Pair(userId, role)
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi đăng nhập: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Đăng ký
    fun register(email: String, fullName: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(email, fullName, password)
            result.onSuccess { userId ->
                _successMessage.value = "Đăng ký thành công! Vui lòng đăng nhập."
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi đăng ký: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Cập nhật tiền tích lũy
    fun updateAccumulatedMoney(userId: String, amount: Double) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.updateAccumulatedMoney(userId, amount)
            result.onSuccess {
                _successMessage.value = "Cập nhật tiền tích lũy thành công!"
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

