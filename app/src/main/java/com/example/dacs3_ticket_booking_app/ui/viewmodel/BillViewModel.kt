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

    private val _billDetail = MutableLiveData<Bill?>()
    val billDetail: LiveData<Bill?> = _billDetail

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
    fun updateStatusBill(billId:String, status: String) {
        _isLoading.value=true
        viewModelScope.launch {
            val result = billRepository.updateBillStatus(billId, "paid")
            result.onSuccess {
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error updating bill: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    // ✅ Thêm từng ghế vào danh sách booked (gọi từng ghế một)
    fun bookSeat(showtimeId: String, seatPosition: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.addBookedSeat(showtimeId, seatPosition)
            result.onSuccess {
                _successMessage.value = "Ghế $seatPosition được xác nhận"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error booking seat: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    // ✅ Cũ: bookSeats (thêm nhiều ghế) - GIỮ cho compatibility
    fun bookSeats(showtimeId: String, seatPositions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            // Gọi từng ghế một
            for (position in seatPositions) {
                val result = showtimeRepository.addBookedSeat(showtimeId, position)
                result.onFailure { e ->
                    _errorMessage.value = "Error booking seat $position: ${e.message}"
                    _isLoading.value = false
                    return@launch
                }
            }
            _successMessage.value = "Tất cả ghế được xác nhận"
            _isLoading.value = false
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

    // ✅ Xóa từng ghế khỏi danh sách booked (gọi từng ghế một)
    fun releaseSeat(showtimeId: String, seatPosition: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.removeBookedSeat(showtimeId, seatPosition)
            result.onSuccess {
                _successMessage.value = "Ghế $seatPosition được giải phóng"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error releasing seat: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    // ✅ Cũ: releaseSeats (xóa nhiều) - GIỮ cho compatibility
    fun releaseSeats(showtimeId: String, seatPositions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            // Gọi từng ghế một
            for (position in seatPositions) {
                val result = showtimeRepository.removeBookedSeat(showtimeId, position)
                result.onFailure { e ->
                    _errorMessage.value = "Error releasing seat $position: ${e.message}"
                    _isLoading.value = false
                    return@launch
                }
            }
            _successMessage.value = "Tất cả ghế được giải phóng"
            _isLoading.value = false
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

    fun getBillById(billId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.getBillById(billId)
            result.onSuccess { bill ->
                _billDetail.value = bill
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading bill: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Delete bill (for payment failure)
    fun deleteBill(billId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = billRepository.deleteBill(billId)
            result.onSuccess {
                _successMessage.value = "Bill deleted successfully"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error deleting bill: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
