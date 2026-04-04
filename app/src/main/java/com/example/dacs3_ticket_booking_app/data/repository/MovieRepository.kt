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
            val snapshot = movieCollection.get().await()
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
}