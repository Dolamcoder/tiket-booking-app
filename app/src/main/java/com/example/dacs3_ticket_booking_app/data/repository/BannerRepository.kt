package com.example.dacs3_ticket_booking_app.data.repository

import com.example.dacs3_ticket_booking_app.data.model.Banner
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BannerRepository {

    private val bannerRef = FirebaseFirestore.getInstance().collection("banners")

    // ✅ CREATE
    suspend fun addBanner(banner: Banner): Result<String> {
        return try {
            val doc = bannerRef.document()
            val newBanner = banner.copy(id = doc.id)

            doc.set(newBanner).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ READ
    suspend fun getBanners(): Result<List<Banner>> {
        return try {
            val snapshot = bannerRef.get().await()
            val list = snapshot.documents.map {
                it.toObject(Banner::class.java)!!
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ UPDATE
    suspend fun updateBanner(banner: Banner): Result<Unit> {
        return try {
            bannerRef.document(banner.id).set(banner).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ DELETE
    suspend fun deleteBanner(id: String): Result<Unit> {
        return try {
            bannerRef.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}