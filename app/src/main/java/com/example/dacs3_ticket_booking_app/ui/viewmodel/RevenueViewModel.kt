package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Revenue
import com.example.dacs3_ticket_booking_app.data.repository.RevenueRepository
import kotlinx.coroutines.launch

class RevenueViewModel : ViewModel() {
    private val revenueRepository = RevenueRepository()

    private val _revenues = MutableLiveData<List<Revenue>>()
    val revenues: LiveData<List<Revenue>> = _revenues

    private val _totalRevenue = MutableLiveData<Double>()
    val totalRevenue: LiveData<Double> = _totalRevenue

    private val _totalTickets = MutableLiveData<Int>()
    val totalTickets: LiveData<Int> = _totalTickets

    private val _revenueByMovie = MutableLiveData<Map<String, Double>>()
    val revenueByMovie: LiveData<Map<String, Double>> = _revenueByMovie

    private val _revenueByDate = MutableLiveData<Map<Long, Double>>()
    val revenueByDate: LiveData<Map<Long, Double>> = _revenueByDate

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // ✅ Lấy tất cả doanh thu
    fun getAllRevenues() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = revenueRepository.getAllRevenues()
            result.onSuccess { list ->
                _revenues.value = list
                _totalRevenue.value = revenueRepository.calculateTotalRevenue(list)
                _totalTickets.value = revenueRepository.calculateTotalTickets(list)
                _revenueByMovie.value = revenueRepository.groupRevenueByMovie(list)
                _revenueByDate.value = revenueRepository.groupRevenueByDate(list)
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải doanh thu: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Lấy doanh thu theo phim
    fun getRevenueByMovie(movieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = revenueRepository.getRevenueByMovie(movieId)
            result.onSuccess { list ->
                _revenues.value = list
                _totalRevenue.value = revenueRepository.calculateTotalRevenue(list)
                _totalTickets.value = revenueRepository.calculateTotalTickets(list)
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Lấy doanh thu theo khoảng ngày
    fun getRevenueByDateRange(startDate: Long, endDate: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = revenueRepository.getRevenueByDateRange(startDate, endDate)
            result.onSuccess { list ->
                _revenues.value = list
                _totalRevenue.value = revenueRepository.calculateTotalRevenue(list)
                _totalTickets.value = revenueRepository.calculateTotalTickets(list)
                _revenueByMovie.value = revenueRepository.groupRevenueByMovie(list)
                _revenueByDate.value = revenueRepository.groupRevenueByDate(list)
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
}

