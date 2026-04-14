package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val movieRepository = MovieRepository()

    // LiveData for all movies
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    // LiveData for single movie detail
    private val _movieDetail = MutableLiveData<Movie?>()
    val movieDetail: LiveData<Movie?> = _movieDetail

    // LiveData for now showing movies
    private val _nowShowingMovies = MutableLiveData<List<Movie>>()
    val nowShowingMovies: LiveData<List<Movie>> = _nowShowingMovies

    // LiveData for coming soon movies
    private val _comingSoonMovies = MutableLiveData<List<Movie>>()
    val comingSoonMovies: LiveData<List<Movie>> = _comingSoonMovies

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData for success message
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Get all movies
    fun getAllMovies() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getAllMovies()
            result.onSuccess { list ->
                _movies.value = list
                // Separate movies by status
                _nowShowingMovies.value = list.filter { it.status == "now_showing" }
                _comingSoonMovies.value = list.filter { it.status == "coming_soon" }
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Get movie by ID
    fun getMovieById(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getMovieById(id)
            result.onSuccess { movie ->
                if (movie != null) {
                    _movieDetail.value = movie
                    _movies.value = listOf(movie)
                }
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Add new movie
    fun addMovie(movie: Movie) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.addMovie(movie)
            result.onSuccess { id ->
                _successMessage.value = "Thêm phim thành công (ID: $id)"
                _isLoading.value = false
                getAllMovies()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi thêm phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Update movie
    fun updateMovie(movie: Movie) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.updateMovie(movie)
            result.onSuccess {
                _successMessage.value = "Cập nhật phim thành công"
                _isLoading.value = false
                getAllMovies()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Delete movie
    fun deleteMovie(movieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.deleteMovie(movieId)
            result.onSuccess {
                _successMessage.value = "Xóa phim thành công"
                _isLoading.value = false
                getAllMovies()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi xóa phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Get movies by status
    fun getMoviesByStatus(status: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getMoviesByStatus(status)
            result.onSuccess { list ->
                _movies.value = list
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi lọc phim theo trạng thái: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // Get movies by genre
    fun getMoviesByGenre(genre: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getMoviesByGenre(genre)
            result.onSuccess { list ->
                _movies.value = list
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi lọc phim theo thể loại: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // 🔍 Tìm kiếm phim theo tiêu đề
    fun searchMoviesByTitle(query: String) {
        val currentMovies = _movies.value ?: return
        val filtered = movieRepository.searchMoviesByTitle(currentMovies, query)
        _movies.value = filtered
    }

    // 📊 Sắp xếp phim theo tên
    fun sortMoviesByTitle(ascending: Boolean = true) {
        val currentMovies = _movies.value ?: return
        val sorted = movieRepository.sortMoviesByTitle(currentMovies, ascending)
        _movies.value = sorted
    }

    // 📊 Sắp xếp phim theo năm
    fun sortMoviesByYear(ascending: Boolean = false) {
        val currentMovies = _movies.value ?: return
        val sorted = movieRepository.sortMoviesByYear(currentMovies, ascending)
        _movies.value = sorted
    }

    // 📊 Sắp xếp phim theo thời lượng
    fun sortMoviesByDuration(ascending: Boolean = true) {
        val currentMovies = _movies.value ?: return
        val sorted = movieRepository.sortMoviesByDuration(currentMovies, ascending)
        _movies.value = sorted
    }

    // 📊 Cập nhật số lượng vé bán cho phim
    fun updateTicketsSold(movieId: String, quantity: Int) {
        viewModelScope.launch {
            val result = movieRepository.updateTicketsSold(movieId, quantity)
            result.onSuccess {
                getAllMovies()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật số vé: ${exception.message}"
            }
        }
    }

    // 💰 Cập nhật doanh thu cho phim
    fun updateRevenue(movieId: String, revenue: Double) {
        viewModelScope.launch {
            val result = movieRepository.updateRevenue(movieId, revenue)
            result.onSuccess {
                getAllMovies()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật doanh thu: ${exception.message}"
            }
        }
    }

    // 📊 Sắp xếp phim theo số vé bán (nhiều nhất trước)
    fun sortMoviesByTicketsSold(descending: Boolean = true) {
        val currentMovies = _movies.value ?: return
        val sorted = movieRepository.sortMoviesByTicketsSold(currentMovies, descending)
        _movies.value = sorted
    }

    // 📊 Sắp xếp phim theo doanh thu (cao nhất trước)
    fun sortMoviesByRevenue(descending: Boolean = true) {
        val currentMovies = _movies.value ?: return
        val sorted = movieRepository.sortMoviesByRevenue(currentMovies, descending)
        _movies.value = sorted
    }
}
