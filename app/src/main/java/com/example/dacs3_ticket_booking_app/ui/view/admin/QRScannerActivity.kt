package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.databinding.ActivityQrScannerBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.QRViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject

class QRScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var qrViewModel: QRViewModel
    private lateinit var billViewModel: BillViewModel
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        setupViewModels()
        setupListeners()
        observeViewModel()
        startQRScanner()
    }

    private fun setupViewModels() {
        qrViewModel = ViewModelProvider(this).get(QRViewModel::class.java)
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        db = FirebaseFirestore.getInstance()
    }

    private fun setupListeners() {
        binding.scanQrBtn.setOnClickListener {
            startQRScanner()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun startQRScanner() {
        Log.d("QRScannerActivity", "📱 Starting QR scanner...")
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Quét vé QR code")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("QRScannerActivity", "❌ Quét bị hủy")
                Toast.makeText(this, "Quét bị hủy", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("QRScannerActivity", "✅ QR scanned: ${result.contents}")
                handleQRCodeData(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQRCodeData(qrString: String) {
        try {
            val qrJson = JSONObject(qrString)
            val billId = qrJson.getString("billId")
            val endTime = qrJson.getLong("endTime")
            val signature = qrJson.getString("signature")

            Log.d("QRScannerActivity", "📊 QR Data: billId=$billId, endTime=$endTime, signature=$signature")

            // ✅ Verify QR code
            binding.statusTxt.text = "Đang xác nhận vé..."
            qrViewModel.verifyQR(billId, endTime, signature)
        } catch (e: Exception) {
            Log.e("QRScannerActivity", "❌ Error parsing QR: ${e.message}")
            binding.statusTxt.text = "❌ Lỗi: Mã QR không hợp lệ"
            binding.statusTxt.setTextColor(resources.getColor(android.R.color.holo_red_light))
        }
    }

    private fun observeViewModel() {
        // Theo dõi kết quả verify QR
        qrViewModel.verifyResult.observe(this) { result ->
            if (result != null) {
                if (result.valid) {
                    Log.d("QRScannerActivity", "✅ QR valid: ${result.message}")
                    handleValidQR(result.message)
                } else {
                    Log.d("QRScannerActivity", "❌ QR invalid: ${result.message}")
                    binding.statusTxt.text = "❌ ${result.message}"
                    binding.statusTxt.setTextColor(resources.getColor(android.R.color.holo_red_light))
                }
            }
        }

        qrViewModel.errorMessage.observe(this) { msg ->
            Log.e("QRScannerActivity", "❌ Error: $msg")
            binding.statusTxt.text = "❌ Lỗi: $msg"
            binding.statusTxt.setTextColor(resources.getColor(android.R.color.holo_red_light))
        }

        billViewModel.successMessage.observe(this) { msg ->
            Log.d("QRScannerActivity", "✅ Bill updated: $msg")
            binding.statusTxt.text = "✅ Vé đã được xác nhận"
            binding.statusTxt.setTextColor(resources.getColor(android.R.color.holo_green_light))
            
            // Tự động quét lại sau 2 giây
            binding.root.postDelayed({
                binding.statusTxt.text = "Sẵn sàng quét vé tiếp theo..."
                binding.statusTxt.setTextColor(resources.getColor(android.R.color.black))
                startQRScanner()
            }, 2000)
        }
    }

    private fun handleValidQR(message: String) {
        Log.d("QRScannerActivity", "✅ Handling valid QR: $message")
        binding.statusTxt.text = "✅ Vé hợp lệ! Đang xử lý..."
        binding.statusTxt.setTextColor(resources.getColor(android.R.color.holo_green_light))

        // Extract billId từ message (format: "QR hợp lệ" hoặc message khác)
        // Bạn có thể lấy billId từ QR data đã scan
        // Ở đây ta cần cập nhật status bill thành "verified" hoặc tương tự
        
        Toast.makeText(this, "✅ Vé được chấp nhận", Toast.LENGTH_SHORT).show()
    }
}

