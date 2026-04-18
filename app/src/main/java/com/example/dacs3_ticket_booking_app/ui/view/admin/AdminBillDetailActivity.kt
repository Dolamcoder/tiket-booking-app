package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminBillDetailBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.UserViewModel
import com.example.dacs3_ticket_booking_app.utils.PriceManager
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class AdminBillDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBillDetailBinding
    private lateinit var billViewModel: BillViewModel
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var userViewModel: UserViewModel
    
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBillDetailBinding.inflate(layoutInflater)
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
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private fun setupUI() {
        binding.backBtn.setOnClickListener { finish() }
        observeViewModel()
    }

    private fun observeViewModel() {
        billViewModel.billDetail.observe(this) { bill ->
            if (bill != null) {
                binding.apply {
                    // Bill header
                    tvBillId.text = "Bill #${bill.id}"
                    tvBookingDate.text = "Ngày đặt: ${sdf.format(Date(bill.bookingTime))}"
                    
                    tvStatus.text = "Trạng thái: ${bill.status.uppercase()}"
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
                    userViewModel.getUserById(bill.userId)
                    
                    // Display seats
                    val seatNames = bill.seatPositions.map { SeatUtils.positionToDisplay(it) }
                    tvSeats.text = "Ghế: ${seatNames.joinToString(", ")}"
                    
                    // Display pricing
                    val totalPrice = bill.price * bill.seatPositions.size
                    tvUnitPrice.text = "Giá/ghế: ${PriceManager.formatPrice(bill.price)}"
                    tvQuantity.text = "Số lượng: ${bill.seatPositions.size}"
                    tvTotalPrice.text = "Tổng cộng: ${PriceManager.formatPrice(totalPrice)}"
                }
            }
        }

        movieViewModel.movieDetail.observe(this) { movie ->
            if (movie != null) {
                binding.apply {
                    tvMovieTitle.text = movie.title
                    tvYear.text = "Năm: ${movie.year}"
                    tvGenres.text = "Thể loại: ${movie.genres.joinToString(", ")}"
                    
                    if (movie.poster.isNotEmpty()) {
                        Glide.with(this@AdminBillDetailActivity)
                            .load(movie.poster)
                            .into(ivPoster)
                    }
                }
            }
        }

        showtimeViewModel.showtimeDetail.observe(this) { showtime ->
            if (showtime != null) {
                binding.apply {
                    tvScreeningDate.text = "Ngày chiếu: ${showtime.screeningDate}"
                    tvTimeSlot.text = "Khung giờ: ${showtime.timeSlot}"
                }
            }
        }

        roomViewModel.roomDetail.observe(this) { room ->
            if (room != null) {
                binding.tvRoom.text = "Phòng chiếu: ${room.name}"
            }
        }

        userViewModel.userDetail.observe(this) { user ->
            if (user != null) {
                binding.tvCustomer.text = "Khách hàng: ${user.fullName}"
                binding.tvCustomerEmail.text = "Email: ${user.email}"
            }
        }

        billViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        billViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        billViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

}



