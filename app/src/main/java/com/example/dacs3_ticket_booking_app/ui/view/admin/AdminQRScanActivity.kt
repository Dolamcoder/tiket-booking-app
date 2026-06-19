package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminQrScanBinding
import com.example.dacs3_ticket_booking_app.data.api.RetrofitClient
import com.example.dacs3_ticket_booking_app.data.api.VerifyQRRequest
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AdminQRScanActivity : AppCompatActivity(), BarcodeCallback {

    private lateinit var binding: ActivityAdminQrScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAdminQrScanBinding.inflate(layoutInflater)

        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        setupUI()

        checkCameraPermission()
    }

    private fun setupUI() {

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startScanner()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    private fun startScanner() {

        binding.barcodeScanner.decodeContinuous(this)

        binding.barcodeScanner.resume()
    }

    override fun barcodeResult(result: BarcodeResult?) {
        result?.text?.let { qrText ->
            // Pause scanner while processing
            binding.barcodeScanner.pause()
            
            try {
                // Parse JSON from QR code
                val jsonObject = JSONObject(qrText)
                val billId = jsonObject.getString("billId")
                val endTime = jsonObject.getLong("endTime")
                val signature = jsonObject.getString("signature")
                val count = jsonObject.optLong("count", 1) // Default to 1 if not present
                // Verify QR code with backend
                verifyQRCode(billId, endTime, signature,count )
            } catch (e: Exception) {
                // If JSON parsing fails, show error
                showErrorDialog("Mã QR không hợp lệ", "Không thể đọc dữ liệu từ mã QR: ${e.message}") {
                    binding.barcodeScanner.resume()
                }
            }
        }
    }

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

    }

    override fun onResume() {
        super.onResume()

        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()

        binding.barcodeScanner.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            startScanner()

        } else {

            Toast.makeText(
                this,
                "Không có quyền camera",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun verifyQRCode(billId: String, endTime: Long, signature: String, count:Long) {
        MainScope().launch {
            try {
                val request = VerifyQRRequest(billId, endTime, signature, count)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.qrService.verifyQR(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()!!
                    if (body.valid) {
                        val message = """ ${body.message} \n Số vé đã đặt: ${body.count}""".trimIndent()
                        showSuccessDialog(message) {
                            binding.barcodeScanner.resume()
                        }
                    } else {
                        // QR is invalid or expired
                        showErrorDialog("QR Không Hợp Lệ", body.message) {
                            binding.barcodeScanner.resume()
                        }
                    }
                } else {

                    val errorBody = response.errorBody()?.string()

                    var errorMessage = "Lỗi xác minh mã QR"

                    try {
                        if (!errorBody.isNullOrEmpty()) {
                            val json = JSONObject(errorBody)

                            errorMessage =
                                json.optString(
                                    "message",
                                    json.optString(
                                        "error",
                                        "Lỗi xác minh mã QR"
                                    )
                                )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    showErrorDialog(
                        "QR Không Hợp Lệ",
                        errorMessage
                    ) {
                        binding.barcodeScanner.resume()
                    }
                }
            } catch (e: Exception) {
                showErrorDialog("Lỗi Kết Nối", "Không thể kết nối tới máy chủ: ${e.message}") {
                    binding.barcodeScanner.resume()
                }
            }
        }
    }

    private fun showSuccessDialog(message: String, onDismiss: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("✓ Xác Minh Thành Công")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                onDismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(title: String, message: String, onDismiss: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("✗ $title")
            .setMessage(message)
            .setPositiveButton("Thử Lại") { _, _ ->
                onDismiss()
            }
            .setNegativeButton("Thoát") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}