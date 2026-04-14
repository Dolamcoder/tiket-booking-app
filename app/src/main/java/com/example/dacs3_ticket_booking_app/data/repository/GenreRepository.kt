package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GenreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val genreCollection = db.collection("genres")

    // ✅ Thêm thể loại (auto ID)
    suspend fun addGenre(genre: Genre): Result<String> {
        return try {
            val docRef = genreCollection.document()
            val genreWithId = genre.copy(id = docRef.id)

            docRef.set(genreWithId).await()
            Result.success(docRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy tất cả thể loại
    suspend fun getAllGenres(): Result<List<Genre>> {
        return try {
            val snapshot = genreCollection.get().await()
            val genres = snapshot.toObjects(Genre::class.java)
            Result.success(genres)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lấy thể loại theo ID
    suspend fun getGenreById(id: String): Result<Genre?> {
        return try {
            val doc = genreCollection.document(id).get().await()
            val genre = doc.toObject(Genre::class.java)
            Result.success(genre)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Update thể loại
    suspend fun updateGenre(genre: Genre): Result<Unit> {
        return try {
            genreCollection.document(genre.id)
                .set(genre)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Xóa thể loại
    suspend fun deleteGenre(genreId: String): Result<Unit> {
        return try {
            genreCollection.document(genreId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔍 Tìm kiếm thể loại theo tên (Client-side filtering)
    fun searchGenresByName(genres: List<Genre>, query: String): List<Genre> {
        return genres.filter { it.name.contains(query, ignoreCase = true) }
    }

    // 📊 Sắp xếp thể loại theo tên (A-Z)
    fun sortGenresByName(genres: List<Genre>, ascending: Boolean = true): List<Genre> {
        return if (ascending) {
            genres.sortedBy { it.name }
        } else {
            genres.sortedByDescending { it.name }
        }
    }
}

