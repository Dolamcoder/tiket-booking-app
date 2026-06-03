package com.example.dacs3_ticket_booking_app.utils

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import java.util.Locale

class SpeechToTextUtil(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit = {}
) {

    companion object {
        private const val TAG = "SpeechToTextUtil"
    }

    fun startListening(launcher: ActivityResultLauncher<Intent>) {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

            // Nhận diện tự do
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Ưu tiên tiếng Việt
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale("vi", "VN")
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "vi-VN"
            )

            putExtra(
                RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
                "vi-VN"
            )

            // Prompt hiển thị
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "🎤 Nói tên phim bạn muốn tìm..."
            )

            // Lấy nhiều kết quả để tăng độ chính xác
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )

            // Cho phép kết quả tạm thời
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            // Ưu tiên tiếng Việt
            putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.packageName
            )
        }

        try {
            launcher.launch(intent)
            Log.d(TAG, "Đã bắt đầu nhận diện giọng nói tiếng Việt")
        } catch (e: Exception) {
            Log.e(TAG, "không thể khởi động Speech Recognition", e)
            onError("Không thể khởi động nhận diện giọng nói: ${e.message}")
        }
    }

    fun handleSpeechResult(
        resultCode: Int,
        data: Intent?
    ) {

        when (resultCode) {

            android.app.Activity.RESULT_OK -> {

                if (data == null) {
                    onError("Không nhận được dữ liệu giọng nói")
                    return
                }

                val results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                if (!results.isNullOrEmpty()) {

                    val recognizedText = results.first()

                    Log.d(
                        TAG,
                        "Nhận diện thành công: $recognizedText"
                    )

                    onResult(recognizedText)

                } else {
                    onError("Không nhận diện được nội dung giọng nói")
                }
            }

            android.app.Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Người dùng đã hủy nhận diện")
                onError("Đã hủy nhận diện giọng nói")
            }

            else -> {
                Log.e(
                    TAG,
                    "Speech Recognition lỗi: $resultCode"
                )
                onError("Nhận diện giọng nói thất bại")
            }
        }
    }

    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}