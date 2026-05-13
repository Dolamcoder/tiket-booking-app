package com.example.dacs3_ticket_booking_app.ui.view.client

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.databinding.ActivityUserBillDetailBinding
import com.example.dacs3_ticket_booking_app.data.api.GenerateQRRequest
import com.example.dacs3_ticket_booking_app.data.api.RetrofitClient
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.utils.PriceManager
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*

class UserBillDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBillDetailBinding
    private lateinit var billViewModel: BillViewModel
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var roomViewModel: RoomViewModel
    
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserBillDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        setupViewModels()
        setupUI()
        
        // Get bill ID from intent
        val billId = intent.getStringExtra("BILL_ID")
        if (billId != null) {
            billViewModel.getBillById(billId)
        } else {
            Toast.makeText(this, "Error: Bill not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupViewModels() {
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)
    }

    private fun setupUI() {
        binding.backBtn.setOnClickListener { finish() }
        
        // Set up QR code container click listener
        binding.qrCodeContainer.setOnClickListener {
            val bill = billViewModel.billDetail.value
            if (bill != null) {
                generateAndDisplayQRCode(bill.id)
            } else {
                Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up cancel bill button click listener
        binding.btnCancelBill.setOnClickListener {
            val bill = billViewModel.billDetail.value
            if (bill != null) {
                showCancelConfirmationDialog(bill.id)
            } else {
                Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
        
        observeBillViewModel()
    }

    private fun observeBillViewModel() {
        // Theo dõi chi tiết bill
        billViewModel.billDetail.observe(this) { bill ->
            if (bill != null) {
                binding.apply {
                    // Bill header
                    tvBillId.text = "Bill #${bill.id.take(8)}"
                    tvBookingDate.text = sdf.format(Date(bill.bookingTime))
                    
                    tvStatus.text = bill.status.uppercase()
                    tvStatus.setTextColor(
                        when (bill.status) {
                            "paid" -> resources.getColor(android.R.color.holo_green_light, null)
                            "cancelled" -> resources.getColor(android.R.color.holo_red_light, null)
                            else -> resources.getColor(android.R.color.white, null)
                        }
                    )
                    
                    // Load related data
                    movieViewModel.getMovieByShowtimeId(bill.showtimeId)
                    showtimeViewModel.getShowtimeById(bill.showtimeId)
                    roomViewModel.getRoomByShowtimeId(bill.showtimeId)
                    
                    // Display seats
                    val seatNames = bill.seatPositions.map { SeatUtils.positionToDisplay(it) }
                    tvSeats.text = "Ghế: ${seatNames.joinToString(", ")}"
                    
                    // Display pricing
                    val totalPrice = bill.price * bill.seatPositions.size
                    tvUnitPrice.text = "Giá/ghế: ${PriceManager.formatPrice(bill.price)}"
                    tvQuantity.text = "Số lượng: ${bill.seatPositions.size}"
                    tvTotalPrice.text = "Tổng cộng: ${PriceManager.formatPrice(totalPrice)}"
                    
                    // QR Code (if available)
                    if (bill.qrCodeData.isNotEmpty()) {
                        // TODO: Display QR code from qrCodeData
                        tvQrPlaceholder.visibility = View.GONE
                    }
                }
            }
        }

        // Theo dõi movie
        movieViewModel.movieDetail.observe(this) { movie ->
            if (movie != null) {
                binding.apply {
                    tvMovieTitle.text = movie.title
                    tvYear.text = "Năm: ${movie.year}"
                    tvGenres.text = "Thể loại: ${movie.genres.joinToString(", ")}"
                    tvDuration.text = "Thời lượng: ${movie.duration} phút"
                    
                    // Load poster
                    if (movie.poster.isNotEmpty()) {
                        Glide.with(this@UserBillDetailActivity)
                            .load(movie.poster)
                            .into(ivPoster)
                    }
                }
            }
        }

        // Theo dõi showtime
        showtimeViewModel.showtimeDetail.observe(this) { showtime ->
            if (showtime != null) {
                binding.apply {
                    tvScreeningDate.text = "Ngày chiếu: ${showtime.screeningDate}"
                    tvTimeSlot.text = "Khung giờ: ${showtime.timeSlot}"
                    tvPriceTier.text = "Loại vé: ${showtime.priceTier}"
                }
            }
        }

        // Theo dõi room
        roomViewModel.roomDetail.observe(this) { room ->
            if (room != null) {
                binding.tvRoom.text = "Phòng chiếu: ${room.name}"
            }
        }

        billViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        billViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAndDisplayQRCode(billId: String) {
        MainScope().launch {
            try {
                // Show loading
                binding.qrProgressBar.visibility = View.VISIBLE
                binding.tvQrPlaceholder.visibility = View.GONE

                val bill = billViewModel.billDetail.value ?: return@launch
                
                // Calculate endTime (24 hours from booking time)
                val endTime = bill.bookingTime + (24 * 60 * 60 * 1000)

                // Call backend API to generate QR code
                val request = GenerateQRRequest(billId, endTime)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.qrService.generateQR(request)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val qrImage = response.body()?.qrImage
                    if (!qrImage.isNullOrEmpty()) {
                        // Decode base64 and display
                        try {
                            val decodedBytes = Base64.decode(qrImage.substringAfter(","), Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            
                            binding.apply {
                                ivQrCode.setImageBitmap(bitmap)
                                ivQrCode.visibility = View.VISIBLE
                                tvQrPlaceholder.visibility = View.GONE
                                qrProgressBar.visibility = View.GONE
                            }
                            
                            // Save QR data to bill
                            billViewModel.updateBillQRData(billId, qrImage)
                        } catch (e: Exception) {
                            Toast.makeText(this@UserBillDetailActivity, 
                                "Error displaying QR code: ${e.message}", 
                                Toast.LENGTH_SHORT).show()
                            binding.apply {
                                qrProgressBar.visibility = View.GONE
                                tvQrPlaceholder.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        Toast.makeText(this@UserBillDetailActivity, 
                            "QR code data is empty", 
                            Toast.LENGTH_SHORT).show()
                        binding.apply {
                            qrProgressBar.visibility = View.GONE
                            tvQrPlaceholder.visibility = View.VISIBLE
                        }
                    }
                } else {
                    val errorMsg = response.body()?.error ?: "Failed to generate QR code"
                    Toast.makeText(this@UserBillDetailActivity, 
                        errorMsg, 
                        Toast.LENGTH_SHORT).show()
                    binding.apply {
                        qrProgressBar.visibility = View.GONE
                        tvQrPlaceholder.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                binding.apply {
                    qrProgressBar.visibility = View.GONE
                    tvQrPlaceholder.visibility = View.VISIBLE
                }
                Toast.makeText(this@UserBillDetailActivity, 
                    "Error: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCancelConfirmationDialog(billId: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác Nhận Hủy Hóa Đơn")
            .setMessage("Bạn có chắc chắn muốn hủy hóa đơn này không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Có, Hủy Ngay") { _, _ ->
                cancelBill(billId)
            }
            .setNegativeButton("Không, Giữ Lại", null)
            .show()
    }

    private fun cancelBill(billId: String) {
        billViewModel.cancelBill(billId)
        
        // Observe success and finish activity
        billViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
