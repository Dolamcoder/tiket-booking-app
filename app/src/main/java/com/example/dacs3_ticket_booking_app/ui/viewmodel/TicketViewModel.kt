package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Ticket
import com.example.dacs3_ticket_booking_app.data.repository.TicketRepository
import kotlinx.coroutines.launch

class TicketViewModel : ViewModel() {
    private val ticketRepository = TicketRepository()

    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun getAllTickets() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.getAllTickets()
            result.onSuccess { list ->
                _tickets.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải danh sách vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getTicketsByUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.getTicketsByUser(userId)
            result.onSuccess { list ->
                _tickets.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getTicketsByShowtime(showtimeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.getTicketsByShowtime(showtimeId)
            result.onSuccess { list ->
                _tickets.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addTicket(ticket: Ticket) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.addTicket(ticket)
            result.onSuccess { id ->
                _successMessage.value = "Đặt vé thành công (ID: $id)"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi đặt vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun cancelTicket(ticketId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.updateTicketStatus(ticketId, "cancelled")
            result.onSuccess {
                _successMessage.value = "Đã hủy vé"
                _isLoading.value = false
                getAllTickets()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi hủy vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getTicketsByMovie(movieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = ticketRepository.getTicketsByMovie(movieId)
            result.onSuccess { list ->
                _tickets.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải vé: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
