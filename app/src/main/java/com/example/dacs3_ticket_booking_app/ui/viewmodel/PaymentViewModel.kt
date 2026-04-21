package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val paymentRepository = PaymentRepository()

    private val _paymentUrl = MutableLiveData<String>()
    val paymentUrl: LiveData<String> = _paymentUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _paymentSuccess = MutableLiveData<Boolean>()
    val paymentSuccess: LiveData<Boolean> = _paymentSuccess

    fun initiatePayment(amount: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = paymentRepository.initiatePayment(amount)
            result.onSuccess { payUrl ->
                _paymentUrl.value = payUrl
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi khởi tạo thanh toán: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun checkPaymentStatus(resultCode: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = paymentRepository.checkPaymentStatus(resultCode)
            result.onSuccess { isSuccess ->
                _paymentSuccess.value = isSuccess
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi kiểm tra thanh toán: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}

