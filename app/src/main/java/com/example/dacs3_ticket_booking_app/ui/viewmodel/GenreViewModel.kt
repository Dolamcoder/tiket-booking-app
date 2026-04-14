package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.example.dacs3_ticket_booking_app.data.repository.GenreRepository
import kotlinx.coroutines.launch

class GenreViewModel : ViewModel() {
    private val genreRepository = GenreRepository()

    // LiveData for all genres
    private val _genres = MutableLiveData<List<Genre>>()
    val genres: LiveData<List<Genre>> = _genres

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData for success message
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Get all genres
    fun getAllGenres() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = genreRepository.getAllGenres()
            result.onSuccess { list ->
                _genres.value = list
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải danh sách thể loại: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Add new genre
    fun addGenre(genre: Genre) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = genreRepository.addGenre(genre)
            result.onSuccess { id ->
                _successMessage.value = "Thêm thể loại thành công"
                _isLoading.value = false
                getAllGenres()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi thêm thể loại: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Update genre
    fun updateGenre(genre: Genre) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = genreRepository.updateGenre(genre)
            result.onSuccess {
                _successMessage.value = "Cập nhật thể loại thành công"
                _isLoading.value = false
                getAllGenres()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật thể loại: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Delete genre
    fun deleteGenre(genreId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = genreRepository.deleteGenre(genreId)
            result.onSuccess {
                _successMessage.value = "Xóa thể loại thành công"
                _isLoading.value = false
                getAllGenres()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi xóa thể loại: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // 🔍 Search genre by name
    fun searchGenresByName(query: String) {
        val currentGenres = _genres.value ?: return
        val filtered = genreRepository.searchGenresByName(currentGenres, query)
        _genres.value = filtered
    }

    // 📊 Sort genre by name
    fun sortGenresByName(ascending: Boolean = true) {
        val currentGenres = _genres.value ?: return
        val sorted = genreRepository.sortGenresByName(currentGenres, ascending)
        _genres.value = sorted
    }
}

