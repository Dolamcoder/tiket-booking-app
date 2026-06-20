package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.model.Review
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MovieRepository {

    private val db = FirebaseFirestore.getInstance()
    private val movieCollection = db.collection("movies")

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

    suspend fun getAllMovies(): Result<List<Movie>> {
        return try {
            val snapshot = movieCollection.get().await();
            val movies = snapshot.toObjects(Movie::class.java)
            Result.success(movies)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieById(id: String): Result<Movie?> {
        return try {
            val doc = movieCollection.document(id).get().await()
            val movie = doc.toObject(Movie::class.java)
            Result.success(movie)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    fun searchMoviesByTitle(movies: List<Movie>, query: String): List<Movie> {
        return movies.filter { it.title.contains(query, ignoreCase = true) }
    }

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

    fun sortMoviesByTicketsSold(movies: List<Movie>, descending: Boolean = true): List<Movie> {
        return if (descending) {
            movies.sortedByDescending { it.ticketsSold }
        } else {
            movies.sortedBy { it.ticketsSold }
        }
    }

    fun sortMoviesByRevenue(movies: List<Movie>, descending: Boolean = true): List<Movie> {
        return if (descending) {
            movies.sortedByDescending { it.totalRevenue }
        } else {
            movies.sortedBy { it.totalRevenue }
        }
    }

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

    // --- Review Section ---

    fun listenForReviews(movieId: String, onUpdate: (List<Review>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("movieId", movieId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    onUpdate(reviews)
                }
            }
    }

    suspend fun getReviewsByMovieId(movieId: String): Result<List<Review>> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewCount(movieId: String): Result<Long> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
            Result.success(snapshot.count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaginatedReviews(
        movieId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Result<Pair<List<Review>, DocumentSnapshot?>> {
        return try {
            var query = db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            val reviews = snapshot.toObjects(Review::class.java)
            val newLastDocument = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
            
            Result.success(Pair(reviews, newLastDocument))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReview(review: Review): Result<String> {
        return try {
            val docRef = db.collection("reviews").document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}