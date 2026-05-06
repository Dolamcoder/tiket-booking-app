package com.example.dacs3_ticket_booking_app.utils

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Utility class để handle Google Speech to Text recognition
 * Sử dụng Google Speech Recognition API (miễn phí)
 */
class SpeechToTextUtil(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit = { }
) {
    companion object {
        private const val TAG = "SpeechToTextUtil"
    }

    /**
     * Bắt đầu nhận diện giọng nói
     */
    fun startListening(launcher: ActivityResultLauncher<Intent>) {
        // Bypass kiểm tra isRecognitionAvailable vì nó không chính xác trên một số thiết bị
        // Thay vào đó, ta sẽ xử lý exception nếu có
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi_VN") // Vietnamese
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói tên phim bạn muốn tìm...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            launcher.launch(intent)
            Log.d(TAG, "✅ Bắt đầu nhận diện giọng nói")
        } catch (e: Exception) {
            onError("Lỗi khởi động nhận diện: ${e.message}")
            Log.e(TAG, "❌ Error starting speech recognition", e)
        }
    }

    /**
     * Xử lý kết quả từ Speech Recognition
     */
    fun handleSpeechResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            android.app.Activity.RESULT_OK -> {
                if (data != null) {
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    
                    if (!results.isNullOrEmpty()) {
                        val recognizedText = results[0]
                        Log.d(TAG, "✅ Nhận diện được: $recognizedText")
                        onResult(recognizedText)
                    } else {
                        onError("Không nhận diện được giọng nói")
                    }
                } else {
                    onError("Không có dữ liệu nhận diện")
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                onError("Nhận diện giọng nói bị hủy")
            }
            else -> {
                onError("Nhận diện giọng nói lỗi")
                Log.e(TAG, "❌ Speech recognition failed with result code: $resultCode")
            }
        }
    }

    /**
     * Kiểm tra quyền microphone
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}




