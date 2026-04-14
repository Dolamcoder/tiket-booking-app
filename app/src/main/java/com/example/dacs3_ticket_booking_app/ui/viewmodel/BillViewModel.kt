package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.example.dacs3_ticket_booking_app.data.repository.BillRepository
import com.example.dacs3_ticket_booking_app.data.repository.ShowtimeRepository
import kotlinx.coroutines.launch

class BillViewModel : ViewModel() {
    private val billRepository = BillRepository()
    private val showtimeRepository = ShowtimeRepository()

    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> = _bills

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun getAllBills() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.getAllBills()
            result.onSuccess { list ->
                _bills.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading bills: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getBillsByUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.getBillsByUser(userId)
            result.onSuccess { list ->
                _bills.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading bills: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getBillsByShowtime(showtimeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.getBillsByShowtime(showtimeId)
            result.onSuccess { list ->
                _bills.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading bills: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addBill(bill: Bill) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.addBill(bill)
            result.onSuccess { id ->
                _successMessage.value = "Bill created successfully (ID: $id)"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error creating bill: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Book multiple seats for a showtime
    fun bookSeats(showtimeId: String, seatPositions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.addBookedSeats(showtimeId, seatPositions)
            result.onSuccess {
                _successMessage.value = "Seats booked successfully"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error booking seats: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun cancelBill(billId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.updateBillStatus(billId, "cancelled")
            result.onSuccess {
                _successMessage.value = "Bill cancelled successfully"
                _isLoading.value = false
                getAllBills()
            }
            result.onFailure { e ->
                _errorMessage.value = "Error cancelling bill: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Release multiple booked seats (when cancelling bill)
    fun releaseSeats(showtimeId: String, seatPositions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.removeBookedSeats(showtimeId, seatPositions)
            result.onSuccess {
                _successMessage.value = "Seats released successfully"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error releasing seats: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getBillsByMovie(movieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.getBillsByMovie(movieId)
            result.onSuccess { list ->
                _bills.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading bills: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
