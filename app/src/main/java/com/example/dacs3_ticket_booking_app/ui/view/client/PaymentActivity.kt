package com.example.dacs3_ticket_booking_app.ui.view.client

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.databinding.ActivityPaymentBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.PaymentViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.QRViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel

class
PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var billViewModel: BillViewModel
    private lateinit var showtimeViewModel: ShowtimeViewModel

    companion object {
        const val BILL_ID = "billId"
        const val AMOUNT = "amount"
        const val SHOWTIME_ID = "showtimeId"
        const val SELECTED_SEATS = "selectedSeats"
    }

    private var billId: String = ""
    private var amount: Long = 0L
    private var showtimeId: String = ""
    private var selectedSeats: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentExtras()
        setupViewModels()
        setupWebView()
        observeViewModel()
        initiatePayment()
    }

    private fun getIntentExtras() {
        billId = intent.getStringExtra(BILL_ID) ?: ""
        amount = intent.getLongExtra(AMOUNT, 0L)
        showtimeId = intent.getStringExtra(SHOWTIME_ID) ?: ""
        selectedSeats = intent.getStringArrayListExtra(SELECTED_SEATS) ?: emptyList()

        Log.d("PaymentActivity", "📋 Bill ID: $billId, Amount: $amount, Seats: $selectedSeats")
    }

    private fun setupViewModels() {
        paymentViewModel = ViewModelProvider(this).get(PaymentViewModel::class.java)
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
    }

    private fun setupWebView() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.contains("check-payment")) {
                    Log.d("PaymentActivity", "🔍 Intercepted callback URL: $url")
                    handlePaymentCallback(url)
                    return true  // Prevent default behavior
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("PaymentActivity", "🌐 Page loaded: $url")
                binding.progressBar.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("PaymentActivity", "🌐 Page starting: $url")
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
        }
    }

    private fun observeViewModel() {
        paymentViewModel.paymentUrl.observe(this) { url ->
            if (url.isNotEmpty()) {
                Log.d("PaymentActivity", "✅ Loading payment URL: $url")
                binding.webView.loadUrl(url)
            }
        }

        paymentViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        paymentViewModel.errorMessage.observe(this) { msg ->
            Log.e("PaymentActivity", "❌ Error: $msg")
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }

        paymentViewModel.paymentSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Log.d("PaymentActivity", "✅ Payment successful!")
                android.widget.Toast.makeText(
                    this,
                    "✅ Thanh toán thành công",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Log.d("PaymentActivity", "❌ Payment failed!")
                handlePaymentFailure()
            }
        }
    }

    private fun initiatePayment() {
        // ✅ Gọi API để lấy payment URL
        paymentViewModel.initiatePayment(amount)
    }

    private fun handlePaymentCallback(url: String) {
        Log.d("PaymentActivity", "🔍 Handling payment callback: $url")

        // Parse resultCode từ URL query parameters
        // URL format: http://localhost:3000/api/check-payment?resultCode=0
        val resultCode = try {
            val uri = android.net.Uri.parse(url)
            uri.getQueryParameter("resultCode") ?: "1"
        } catch (e: Exception) {
            "1" // Default to failure
        }

        Log.d("PaymentActivity", "📊 Result Code: $resultCode")

        if (resultCode == "0") {
            // ✅ Payment successful
            Log.d("PaymentActivity", "✅ Payment successful (resultCode=0)")
            handlePaymentSuccess()
        } else {
            // ❌ Payment failed
            Log.d("PaymentActivity", "❌ Payment failed (resultCode=$resultCode)")
            handlePaymentFailure()
        }
    }

    private fun handlePaymentSuccess() {
        Log.d("PaymentActivity", "✅ Processing successful payment...")
        
        // ✅ 1. Update bill status to "paid"
        billViewModel.updateStatusBill(billId, "paid")
        
        // ✅ 2. Confirm booking - thêm ghế vào danh sách booked
        showtimeViewModel.confirmBooking(showtimeId, selectedSeats)

        android.widget.Toast.makeText(
            this,
            "✅ Thanh toán thành công",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        setResult(RESULT_OK)
        finish()
    }

    private fun handlePaymentFailure() {
        Log.d("PaymentActivity", "❌ Processing payment failure...")
        // ❌ Payment failed
        // 1. Xóa Bill
        billViewModel.deleteBill(billId)

        // 2. Unlock ghế
        showtimeViewModel.releaseLockedSeats(showtimeId, selectedSeats)

        android.widget.Toast.makeText(
            this,
            "❌ Thanh toán thất bại. Vui lòng thử lại",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        setResult(RESULT_CANCELED)
        finish()
    }
}




