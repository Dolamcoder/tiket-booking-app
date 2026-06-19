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
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.utils.PriceManager
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
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
            Toast.makeText(
                this,
                "QR đã được tải từ hóa đơn",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // Set up cancel bill button click listener
        binding.btnCancelBill.setOnClickListener {
            val bill = billViewModel.billDetail.value
            val showtime = showtimeViewModel.showtimeDetail.value
            if (bill != null && showtime != null) {
                if (canCancelBill(showtime)) {
                    showCancelConfirmationDialog(bill.id)
                } else {
                    Toast.makeText(this, "Không thể hủy vé sát giờ chiếu (dưới 2 tiếng)!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Dữ liệu chưa tải xong", Toast.LENGTH_SHORT).show()
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
                    
                    val showtime = showtimeViewModel.showtimeDetail.value
                    if (showtime != null) {
                        updateCancelButtonState(bill, showtime)
                    }
                    
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

                        try {

                            val base64Data = bill.qrCodeData.substringAfter(",")

                            val decodedBytes = Base64.decode(
                                base64Data,
                                Base64.DEFAULT
                            )

                            val bitmap = BitmapFactory.decodeByteArray(
                                decodedBytes,
                                0,
                                decodedBytes.size
                            )

                            binding.apply {
                                ivQrCode.setImageBitmap(bitmap)
                                ivQrCode.visibility = View.VISIBLE
                                tvQrPlaceholder.visibility = View.GONE
                                qrProgressBar.visibility = View.GONE
                            }

                        } catch (e: Exception) {

                            e.printStackTrace()

                            binding.apply {
                                ivQrCode.visibility = View.GONE
                                tvQrPlaceholder.visibility = View.VISIBLE
                                qrProgressBar.visibility = View.GONE
                            }
                        }

                    } else {

                        binding.apply {
                            ivQrCode.visibility = View.GONE
                            tvQrPlaceholder.visibility = View.VISIBLE
                            qrProgressBar.visibility = View.GONE
                        }
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
                    
                    // Check cancellation rule
                    val bill = billViewModel.billDetail.value
                    if (bill != null) {
                        updateCancelButtonState(bill, showtime)
                    }
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

    private fun canCancelBill(showtime: com.example.dacs3_ticket_booking_app.data.model.Showtime): Boolean {
        return try {
            val dateStr = showtime.screeningDate // e.g. "24/05/2026"
            val timeStartStr = showtime.timeSlot.split("-").firstOrNull()?.trim() ?: return false // e.g. "08:00"
            val dateTimeStr = "$dateStr $timeStartStr"
            val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val showtimeDate = parser.parse(dateTimeStr) ?: return false
            val currentTime = System.currentTimeMillis()
            val difference = showtimeDate.time - currentTime
            difference >= 2 * 60 * 60 * 1000 // 2 hours in milliseconds
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateCancelButtonState(
        bill: com.example.dacs3_ticket_booking_app.data.model.Bill,
        showtime: com.example.dacs3_ticket_booking_app.data.model.Showtime
    ) {
        binding.apply {
            if (bill.status != "paid") {
                btnCancelBill.visibility = View.GONE
                tvCancelPolicy.visibility = View.GONE
                return
            }
            
            if (canCancelBill(showtime)) {
                btnCancelBill.visibility = View.VISIBLE
                btnCancelBill.isEnabled = true
                btnCancelBill.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark, null))
                tvCancelPolicy.text = "Lưu ý: Vé chỉ có thể hủy trước giờ chiếu 2 tiếng."
                tvCancelPolicy.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                btnCancelBill.visibility = View.VISIBLE
                btnCancelBill.isEnabled = false
                btnCancelBill.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                tvCancelPolicy.text = "Không thể hủy vé này (chỉ được hủy trước giờ chiếu 2 tiếng)."
                tvCancelPolicy.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
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
