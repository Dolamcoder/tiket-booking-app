package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.data.repository.RoomRepository
import kotlinx.coroutines.launch

class RoomViewModel : ViewModel() {
    private val roomRepository = RoomRepository()

    private val _rooms = MutableLiveData<List<Room>>()
    val rooms: LiveData<List<Room>> = _rooms

    private val _roomDetail = MutableLiveData<Room?>()
    val roomDetail: LiveData<Room?> = _roomDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun getAllRooms() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = roomRepository.getAllRooms()
            result.onSuccess { list ->
                _rooms.value = list
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải phòng chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getRoomById(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = roomRepository.getRoomById(id)
            result.onSuccess { room ->
                _roomDetail.value = room
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi tải phòng chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addRoom(room: Room) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = roomRepository.addRoom(room)
            result.onSuccess { id ->
                _successMessage.value = "Thêm phòng chiếu thành công (ID: $id)"
                _isLoading.value = false
                getAllRooms()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi thêm phòng chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateRoom(room: Room) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = roomRepository.updateRoom(room)
            result.onSuccess {
                _successMessage.value = "Cập nhật phòng chiếu thành công"
                _isLoading.value = false
                getAllRooms()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi cập nhật phòng chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteRoom(roomId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = roomRepository.deleteRoom(roomId)
            result.onSuccess {
                _successMessage.value = "Xóa phòng chiếu thành công"
                _isLoading.value = false
                getAllRooms()
            }
            result.onFailure { e ->
                _errorMessage.value = "Lỗi xóa phòng chiếu: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
