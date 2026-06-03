package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MovieRepository {

    private val db = FirebaseFirestore.getInstance()
    private val movieCollection = db.collection("movies")

    // ✅ Thêm phim (auto ID)
    suspend fun addMovie(movie: Movie): Result<String> {
        return try {
            val docRef = movieCollection.document()
            val movieWithId = movie.copy(id = docRef.id)

            docRef.set(movieWithId).await()
            Result.success(docRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy tất cả phim
    suspend fun getAllMovies(): Result<List<Movie>> {
        return try {
            val snapshot = movieCollection.get().await();
            val movies = snapshot.toObjects(Movie::class.java)
            Result.success(movies)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy theo ID
    suspend fun getMovieById(id: String): Result<Movie?> {
        return try {
            val doc = movieCollection.document(id).get().await()
            val movie = doc.toObject(Movie::class.java)
            Result.success(movie)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Update phim
    suspend fun updateMovie(movie: Movie): Result<Unit> {
        return try {
            movieCollection.document(movie.id)
                .set(movie)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa phim
    suspend fun deleteMovie(movieId: String): Result<Unit> {
        return try {
            movieCollection.document(movieId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔥 Lọc theo status
    suspend fun getMoviesByStatus(status: String): Result<List<Movie>> {
        return try {
            val snapshot = movieCollection
                .whereEqualTo("status", status)
                .get()
                .await()

            val movies = snapshot.toObjects(Movie::class.java)
            Result.success(movies)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔥 Lọc theo genre
    suspend fun getMoviesByGenre(genre: String): Result<List<Movie>> {
        return try {
            val snapshot = movieCollection
                .whereArrayContains("genres", genre)
                .get()
                .await()

            val movies = snapshot.toObjects(Movie::class.java)
            Result.success(movies)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔍 Tim kiem phim theo tieu de (Client-side filtering)
    fun searchMoviesByTitle(movies: List<Movie>, query: String): List<Movie> {
        return movies.filter { it.title.contains(query, ignoreCase = true) }
    }

    // 🔍 Tìm kiếm phim nâng cao (Client-side filtering - nhiều tham số)
    fun advancedSearchMovies(
        movies: List<Movie>,
        title: String? = null,
        description: String? = null,
        genres: List<String>? = null,
        castName: String? = null
    ): List<Movie> {
        var filtered = movies

        // Filter theo title (contains, case-insensitive)
        if (!title.isNullOrBlank()) {
            filtered = filtered.filter { it.title.contains(title, ignoreCase = true) }
        }

        // Filter theo description (contains, case-insensitive)
        if (!description.isNullOrBlank()) {
            filtered = filtered.filter { it.description.contains(description, ignoreCase = true) }
        }

        // Filter theo genres - phim phải chứa ÍT NHẤT 1 thể loại được chọn
        if (!genres.isNullOrEmpty()) {
            filtered = filtered.filter { movie ->
                movie.genres.any { genre -> genres.contains(genre) }
            }
        }

        // Filter theo cast name (contains, case-insensitive trong danh sách cast)
        if (!castName.isNullOrBlank()) {
            filtered = filtered.filter { movie ->
                movie.casts.any { cast -> cast.name.contains(castName, ignoreCase = true) }
            }
        }

        return filtered
    }

    // Sort phim theo ten (A-Z)
    fun sortMoviesByTitle(movies: List<Movie>, ascending: Boolean = true): List<Movie> {
        return if (ascending) {
            movies.sortedBy { it.title }
        } else {
            movies.sortedByDescending { it.title }
        }
    }

    // Sort phim theo nam phat hanh
    fun sortMoviesByYear(movies: List<Movie>, ascending: Boolean = false): List<Movie> {
        return if (ascending) {
            movies.sortedBy { it.year }
        } else {
            movies.sortedByDescending { it.year }
        }
    }

    // Sort phim theo thoi luong
    fun sortMoviesByDuration(movies: List<Movie>, ascending: Boolean = true): List<Movie> {
        return if (ascending) {
            movies.sortedBy { it.duration }
        } else {
            movies.sortedByDescending { it.duration }
        }
    }

    // 📊 Cập nhật số lượng vé bán cho phim
    suspend fun updateTicketsSold(movieId: String, quantity: Int): Result<Unit> {
        return try {
            movieCollection.document(movieId).update(
                "ticketsSold", com.google.firebase.firestore.FieldValue.increment(quantity.toLong())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 💰 Cập nhật doanh thu cho phim
    suspend fun updateRevenue(movieId: String, revenue: Double, ticketsSold:Int): Result<Unit> {
        return try {
            movieCollection.document(movieId).update(
                "totalRevenue", com.google.firebase.firestore.FieldValue.increment(revenue),
                "ticketsSold", com.google.firebase.firestore.FieldValue.increment(ticketsSold.toLong())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔄 Sắp xếp phim theo số vé bán (nhiều nhất trước)
    fun sortMoviesByTicketsSold(movies: List<Movie>, descending: Boolean = true): List<Movie> {
        return if (descending) {
            movies.sortedByDescending { it.ticketsSold }
        } else {
            movies.sortedBy { it.ticketsSold }
        }
    }

    // 🔄 Sắp xếp phim theo doanh thu (cao nhất trước)
    fun sortMoviesByRevenue(movies: List<Movie>, descending: Boolean = true): List<Movie> {
        return if (descending) {
            movies.sortedByDescending { it.totalRevenue }
        } else {
            movies.sortedBy { it.totalRevenue }
        }
    }

    // 🎬 Lấy phim theo showtimeId
    suspend fun getMovieByShowtimeId(showtimeId: String): Result<Movie?> {
        return try {
            val showtimeCollection = db.collection("showtimes")
            val showtimeDoc = showtimeCollection.document(showtimeId).get().await()
            val movieId = showtimeDoc.getString("movieId")
            
            if (movieId != null) {
                val movie = movieCollection.document(movieId).get().await().toObject(Movie::class.java)
                Result.success(movie)
            } else {
                Result.failure(Exception("Movie ID not found in showtime"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}