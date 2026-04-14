package com.example.dacs3_ticket_booking_app.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryHelper {

    // Gọi hàm này 1 lần duy nhất trong Application.onCreate() hoặc tại AdminActivity
    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "drmkkrmkw",
            "api_key"    to "282185291775257",
            "api_secret" to "wJSLylOsbT_xxTH081CFcDD_sBE"
        )
        MediaManager.init(context, config)
    }

    /**
     * Upload ảnh lên Cloudinary.
     * Ảnh sẽ lưu ở folder: TicketBooking
     * [onResult] trả về URL (String) nếu thành công, hoặc null nếu lỗi.
     */
    fun uploadImage(uri: Uri, context: Context, onResult: (String?) -> Unit) {
        MediaManager.get()
            .upload(uri)
            .option("folder", "TicketBooking")  // ✅ Folder trên Cloudinary
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    onResult(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onResult(null)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    onResult(null)
                }
            })
            .dispatch(context)
    }
}
