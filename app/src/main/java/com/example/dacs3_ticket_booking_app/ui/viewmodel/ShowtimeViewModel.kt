package com.example.dacs3_ticket_booking_app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.example.dacs3_ticket_booking_app.data.repository.ShowtimeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ShowtimeViewModel : ViewModel() {
    private val showtimeRepository = ShowtimeRepository()

    private val _showtimes = MutableLiveData<List<Showtime>>()
    val showtimes: LiveData<List<Showtime>> = _showtimes

    // 📅 Danh sách ngày chiếu
    private val _screeningDates = MutableLiveData<List<String>>()
    val screeningDates: LiveData<List<String>> = _screeningDates

    // ⏰ Danh sách khung giờ
    private val _timeSlots = MutableLiveData<List<String>>()
    val timeSlots: LiveData<List<String>> = _timeSlots

    // 🎬 Suất chiếu được chọn
    private val _selectedShowtime = MutableLiveData<Showtime?>()
    val selectedShowtime: LiveData<Showtime?> = _selectedShowtime

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _showtimeDetail = MutableLiveData<Showtime?>()
    val showtimeDetail: LiveData<Showtime?> = _showtimeDetail

    fun getAllShowtimes() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.getAllShowtimes()
            result.onSuccess { list ->
                _showtimes.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getShowtimesByMovie(movieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.getShowtimesByMovie(movieId)
            result.onSuccess { list ->
                _showtimes.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getShowtimesByRoom(roomId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.getShowtimesByRoom(roomId)
            result.onSuccess { list ->
                _showtimes.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ✨ NEW: Lấy showtimes theo phòng & ngày (dùng để tìm khung giờ trống)
    fun getShowtimesByRoomAndDate(roomId: String, date: String, callback: (List<Showtime>) -> Unit) {
        viewModelScope.launch {
            val result = showtimeRepository.getShowtimesByRoomAndDate(roomId, date)
            result.onSuccess { list ->
                callback(list)
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải suất chiếu: ${e.message}"
                callback(emptyList())
            }
        }
    }

    fun addShowtime(showtime: Showtime) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.addShowtime(showtime)
            result.onSuccess { id ->
                _successMessage.value = "Thêm suất chiếu thành công (ID: $id)"
                _isLoading.value = false
                getAllShowtimes()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi thêm suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

     fun updateShowtime(showtime: Showtime) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.updateShowtime(showtime)
            result.onSuccess {
                _successMessage.value = "Cập nhật suất chiếu thành công"
                _isLoading.value = false
                getAllShowtimes()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi cập nhật suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteShowtime(showtimeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.deleteShowtime(showtimeId)
            result.onSuccess {
                _successMessage.value = "Xóa suất chiếu thành công"
                _isLoading.value = false
                getAllShowtimes()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi xóa suất chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // 🔒 Lock ghế (chống đặt trùng)
    fun lockSeats(showtimeId: String, positions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.lockSeats(showtimeId, positions)
            result.onSuccess {
                _successMessage.value = "Giữ ghế thành công"
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Không thể giữ ghế: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Xác nhận booking sau thanh toán
    fun confirmBooking(showtimeId: String, positions: List<String>) {
        viewModelScope.launch {
            val result = showtimeRepository.confirmBooking(showtimeId, positions)
            result.onSuccess {
                _successMessage.value = "Đặt ghế thành công"
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi xác nhận ghế: ${e.message}"
            }
        }
    }

    // ❌ Hủy lock khi user cancel / timeout
    fun releaseLockedSeats(showtimeId: String, positions: List<String>) {
        viewModelScope.launch {
            val result = showtimeRepository.releaseLockedSeats(showtimeId, positions)
            result.onSuccess {
                Log.d("ShowtimeViewModel", "✅ Released locked seats: $positions")
            }
            result.onFailure { e ->
                Log.e("ShowtimeViewModel", "❌ Failed to release locked seats: ${e.message}", e)
                _errorMessage.value = "Lỗi nhả lock ghế: ${e.message}"
            }
        }
    }

    // 📅 Load danh sách ngày chiếu theo movieId
    fun loadScreeningDates(movieId: String) {
        _isLoading.value = true

        viewModelScope.launch {

            val showtimesResult = showtimeRepository.getShowtimesByMovie(movieId)

            showtimesResult.onSuccess { showtimes ->

                val formatter = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                )

                val today = formatter.parse(
                    formatter.format(Date())
                )

                val validShowtimes = showtimes.filter {
                    try {
                        val screeningDate = formatter.parse(it.screeningDate)
                        screeningDate != null && !screeningDate.before(today)
                    } catch (e: Exception) {
                        false
                    }
                }

                _showtimes.value = validShowtimes

                val uniqueDates = validShowtimes
                    .map { it.screeningDate }
                    .distinct()
                    .sorted()

                _screeningDates.value = uniqueDates

                _isLoading.value = false
            }

            showtimesResult.onFailure { e ->
                _errorMessage.value = "Lỗi tải ngày chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    // ⏰ Load danh sách khung giờ theo movieId và ngày
    fun loadTimeSlots(movieId: String, screeningDate: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.getTimeSlotsByMovieAndDate(movieId, screeningDate)
            result.onSuccess { slots ->
                _timeSlots.value = slots
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải khung giờ: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // 🎬 Lưu suất chiếu được chọn
    fun selectShowtime(showtime: Showtime) {
        _selectedShowtime.value = showtime
    }

    // 📍 Lấy chi tiết suất chiếu theo ID
    fun getShowtimeById(showtimeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = showtimeRepository.getShowtimeById(showtimeId)
            result.onSuccess { showtime ->
                _showtimeDetail.value = showtime
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading showtime: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
