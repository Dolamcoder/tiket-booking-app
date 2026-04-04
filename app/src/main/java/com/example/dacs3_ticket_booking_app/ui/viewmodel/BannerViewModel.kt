package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Banner
import com.example.dacs3_ticket_booking_app.data.repository.BannerRepository
import kotlinx.coroutines.launch

class BannerViewModel : ViewModel() {
    private val bannerRepository = BannerRepository()

    // LiveData for banners list
    private val _banners = MutableLiveData<List<Banner>>()
    val banners: LiveData<List<Banner>> = _banners

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData for success message
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Get all banners
    fun getBanners() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = bannerRepository.getBanners()
            result.onSuccess { list ->
                _banners.value = list
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải banner: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Add new banner
    fun addBanner(banner: Banner) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = bannerRepository.addBanner(banner)
            result.onSuccess { id ->
                _successMessage.value = "Thêm banner thành công (ID: $id)"
                _isLoading.value = false
                getBanners()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi thêm banner: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Update banner
    fun updateBanner(banner: Banner) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = bannerRepository.updateBanner(banner)
            result.onSuccess {
                _successMessage.value = "Cập nhật banner thành công"
                _isLoading.value = false
                getBanners()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật banner: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Delete banner
    fun deleteBanner(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = bannerRepository.deleteBanner(id)
            result.onSuccess {
                _successMessage.value = "Xóa banner thành công"
                _isLoading.value = false
                getBanners()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi xóa banner: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
}

