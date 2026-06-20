package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.model.Review
import com.example.dacs3_ticket_booking_app.data.repository.MovieRepository
import com.example.dacs3_ticket_booking_app.data.repository.ShowtimeRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val movieRepository = MovieRepository()
    private val showtimeRepository = ShowtimeRepository()

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _movieDetail = MutableLiveData<Movie?>()
    val movieDetail: LiveData<Movie?> = _movieDetail

    private val _nowShowingMovies = MutableLiveData<List<Movie>>()
    val nowShowingMovies: LiveData<List<Movie>> = _nowShowingMovies

    private val _comingSoonMovies = MutableLiveData<List<Movie>>()
    val comingSoonMovies: LiveData<List<Movie>> = _comingSoonMovies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> = _searchResults

    // Phân trang Reviews
    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _totalReviews = MutableLiveData<Int>(0)
    val totalReviews: LiveData<Int> = _totalReviews

    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage

    private val pageSize = 5
    private val pageCursors = mutableMapOf<Int, DocumentSnapshot?>()
    private var isFetchingReviews = false

    fun getAllMovies() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getAllMovies()
            result.onSuccess { list ->
                _movies.value = list
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

    fun getMovieById(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getMovieById(id)
            result.onSuccess { movie ->
                if (movie != null) {
                    _movieDetail.value = movie
                    _movies.value = listOf(movie)
                    getReviewsForMovie(id)
                }
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải phim: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    fun getReviewsForMovie(movieId: String) {
        pageCursors.clear()
        pageCursors[1] = null
        _currentPage.value = 1
        
        viewModelScope.launch {
            val countResult = movieRepository.getReviewCount(movieId)
            countResult.onSuccess { count ->
                _totalReviews.value = count.toInt()
                loadPage(movieId, 1)
            }
        }
    }

    fun loadNextPage(movieId: String) {
        val current = _currentPage.value ?: 1
        val totalPages = Math.ceil((_totalReviews.value ?: 0).toDouble() / pageSize).toInt()
        if (current < totalPages) {
            loadPage(movieId, current + 1)
        }
    }

    fun loadPrevPage(movieId: String) {
        val current = _currentPage.value ?: 1
        if (current > 1) {
            loadPage(movieId, current - 1)
        }
    }

    private fun loadPage(movieId: String, page: Int) {
        if (isFetchingReviews) return
        isFetchingReviews = true

        viewModelScope.launch {
            val cursor = pageCursors[page]
            val result = movieRepository.getPaginatedReviews(movieId, pageSize.toLong(), cursor)
            result.onSuccess { (newReviews, lastDoc) ->
                _reviews.value = newReviews
                _currentPage.value = page
                if (lastDoc != null) {
                    pageCursors[page + 1] = lastDoc
                }
                isFetchingReviews = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải trang $page: ${exception.message}"
                isFetchingReviews = false
            }
        }
    }

    fun addReview(review: Review) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.addReview(review)
            result.onSuccess {
                _successMessage.value = "Đã gửi đánh giá của bạn"
                getReviewsForMovie(review.movieId)
                _isLoading.value = false
            }
            result.onFailure {
                _errorMessage.value = "Lỗi khi gửi đánh giá: ${it.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchMoviesByTitle(query: String) {
        val allMovies = mutableListOf<Movie>().apply {
            addAll(_nowShowingMovies.value ?: emptyList())
            addAll(_comingSoonMovies.value ?: emptyList())
        }
        val filtered = if (query.isEmpty()) allMovies else movieRepository.searchMoviesByTitle(allMovies, query)
        _nowShowingMovies.value = filtered.filter { it.status == "now_showing" }
        _comingSoonMovies.value = filtered.filter { it.status == "coming_soon" }
        _movies.value = filtered
    }

    // Lấy phim theo ID suất chiếu
    fun getMovieByShowtimeId(showtimeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = movieRepository.getMovieByShowtimeId(showtimeId)
            result.onSuccess { movie ->
                _movieDetail.value = movie
                _isLoading.value = false
            }
            result.onFailure { e ->
                _errorMessage.value = "Error loading movie: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Các hàm quản lý phim (Khôi phục lại)
    fun addMovie(movie: Movie) {
        viewModelScope.launch {
            movieRepository.addMovie(movie).onSuccess { getAllMovies() }
        }
    }

    fun updateMovie(movie: Movie) {
        viewModelScope.launch {
            movieRepository.updateMovie(movie).onSuccess { getAllMovies() }
        }
    }

    fun deleteMovie(movieId: String) {
        viewModelScope.launch {
            movieRepository.deleteMovie(movieId).onSuccess { getAllMovies() }
        }
    }

    fun updateRevenue(movieId: String, revenue: Double, ticketsSold: Int) {
        viewModelScope.launch {
            movieRepository.updateRevenue(movieId, revenue, ticketsSold)
        }
    }

    fun advancedSearch(title: String?, description: String?, genres: List<String>?, castName: String?, priceTier: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allMovies = movieRepository.getAllMovies().getOrThrow()
                var filtered = movieRepository.advancedSearchMovies(allMovies, title, description, genres, castName)
                _searchResults.value = filtered
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }
}
