package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.api.GenerateQRResponse
import com.example.dacs3_ticket_booking_app.data.api.VerifyQRResponse
import com.example.dacs3_ticket_booking_app.data.repository.QRRepository
import kotlinx.coroutines.launch
import android.util.Log

class QRViewModel : ViewModel() {
    private val qrRepository = QRRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _qrCodeData = MutableLiveData<GenerateQRResponse?>()
    val qrCodeData: LiveData<GenerateQRResponse?> = _qrCodeData

    private val _verifyResult = MutableLiveData<VerifyQRResponse?>()
    val verifyResult: LiveData<VerifyQRResponse?> = _verifyResult

    fun generateQR(billId: String, endTime: Long) {
        _isLoading.value = true
        Log.d("QRViewModel", "📊 Generating QR: billId=$billId, endTime=$endTime")

        viewModelScope.launch {
            val result = qrRepository.generateQR(billId, endTime)
            result.onSuccess { response ->
                Log.d("QRViewModel", "✅ QR generated: ${response.qrImage?.take(50)}...")
                _qrCodeData.value = response
                _successMessage.value = "QR code generated successfully"
                _isLoading.value = false
            }
            result.onFailure { e ->
                Log.e("QRViewModel", "❌ Error: ${e.message}")
                _errorMessage.value = "Error generating QR: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun verifyQR(billId: String, endTime: Long, signature: String) {
        _isLoading.value = true
        Log.d("QRViewModel", "🔐 Verifying QR: billId=$billId")

        viewModelScope.launch {
            val result = qrRepository.verifyQR(billId, endTime, signature)
            result.onSuccess { response ->
                Log.d("QRViewModel", "✅ Verification result: valid=${response.valid}, message=${response.message}")
                _verifyResult.value = response
                if (response.valid) {
                    _successMessage.value = response.message
                } else {
                    _errorMessage.value = response.message
                }
                _isLoading.value = false
            }
            result.onFailure { e ->
                Log.e("QRViewModel", "❌ Error: ${e.message}")
                _errorMessage.value = "Error verifying QR: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}