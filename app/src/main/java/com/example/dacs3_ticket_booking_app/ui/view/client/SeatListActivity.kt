package com.example.dacs3_ticket_booking_app.ui.view.client

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.data.model.Bill
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.databinding.ActivitySeatListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.DateAdapter
import com.example.dacs3_ticket_booking_app.ui.view.adapter.SeatAdapter
import com.example.dacs3_ticket_booking_app.ui.view.adapter.TimeAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.utils.PriceManager
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import com.google.firebase.auth.FirebaseAuth

class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var movie: Movie
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var billViewModel: BillViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    
    private var selectedScreeningDate: String = ""
    private var selectedTimeSlot: String = ""
    private var selectedRoom: Room? = null
    private var selectedPriceTier: String = "morning"  // ✅ Lưu priceTier từ showtime
    private var price: Double = 0.0
    private var selectedSeatCount: Int = 0
    private var selectedSeatPositions: List<String> = emptyList()  // ✅ Danh sách ghế được chọn
    private var seatAdapter: SeatAdapter? = null  // ✅ Reference đến adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        getIntentExtra()
        setupViewModels()
        backOnClick()
        loadScreeningDates()
        observeViewModel()
    }

    private fun getIntentExtra() {
        movie = intent.getSerializableExtra("movie") as Movie
    }

    private fun setupViewModels() {
        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun backOnClick() {
        binding.backBtn.setOnClickListener { finish() }
        // ✅ Xử lý click nút "Tải Vé"
        binding.btnBooking.setOnClickListener {
            if (selectedSeatCount == 0) {
                android.widget.Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            bookSeats()
        }
    }

    // 📅 Load danh sách ngày chiếu từ Firebase
    private fun loadScreeningDates() {
        binding.apply {
            dateRecyclerView.visibility = View.VISIBLE
            timeRecyclerView.visibility = View.GONE
            seatRecylerView.visibility = View.GONE
        }
        
        showtimeViewModel.loadScreeningDates(movie.id)
    }

    private fun observeViewModel() {
        // Theo dõi danh sách ngày chiếu
        showtimeViewModel.screeningDates.observe(this) { dates ->
            android.util.Log.d("SeatListActivity", "✅ Loaded ${dates.size} screening dates")
            binding.dateRecyclerView.layoutManager = 
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.dateRecyclerView.adapter = DateAdapter(dates) { selectedDate ->
                onDateSelected(selectedDate)
            }
        }

        // Theo dõi danh sách khung giờ
        showtimeViewModel.timeSlots.observe(this) { slots ->
            android.util.Log.d("SeatListActivity", "✅ Loaded ${slots.size} time slots")
            binding.apply {
                timeRecyclerView.visibility = View.VISIBLE
            }
            binding.timeRecyclerView.layoutManager = 
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.timeRecyclerView.adapter = TimeAdapter(slots) { selectedSlot ->
                onTimeSlotSelected(selectedSlot)
            }
        }

        // Theo dõi suất chiếu được chọn
        showtimeViewModel.selectedShowtime.observe(this) { showtime ->
            android.util.Log.d("SeatListActivity", "✅ Selected showtime: ${showtime?.id}")
            if (showtime != null) {
                loadSeatLayout(showtime.roomId)
            }
        }

        // Theo dõi phòng chiếu
        roomViewModel.roomDetail.observe(this) { room ->
            android.util.Log.d("SeatListActivity", "✅ Loaded room: ${room?.name}")
            if (room != null) {
                selectedRoom = room
                displaySeatLayout(room)
            }
        }

        showtimeViewModel.isLoading.observe(this) { isLoading ->
            // TODO: Add ProgressBar to layout if needed
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        showtimeViewModel.errorMessage.observe(this) { msg ->
            android.util.Log.e("SeatListActivity", "❌ Error: $msg")
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDateSelected(screeningDate: String) {
        selectedScreeningDate = screeningDate
        binding.apply {
            seatRecylerView.visibility = View.GONE
        }
        // Load khung giờ cho ngày được chọn
        showtimeViewModel.loadTimeSlots(movie.id, screeningDate)
    }

    private fun onTimeSlotSelected(timeSlot: String) {
        selectedTimeSlot = timeSlot
        binding.apply {
            seatRecylerView.visibility = View.VISIBLE
        }
        
        // Load suất chiếu & phòng chiếu từ danh sách suất chiếu đã load
        val showtimes = showtimeViewModel.showtimes.value ?: emptyList()
        if (showtimes.isEmpty()) {
            android.widget.Toast.makeText(this, "Chưa tải được suất chiếu", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedShowtime = showtimes.find { 
            it.screeningDate == selectedScreeningDate && it.timeSlot == selectedTimeSlot && it.movieId == movie.id
        }
        
        if (selectedShowtime != null) {
            // ✅ Lưu priceTier từ showtime
            selectedPriceTier = selectedShowtime.priceTier
            showtimeViewModel.selectShowtime(selectedShowtime)
        } else {
            android.widget.Toast.makeText(this, "Không tìm thấy suất chiếu", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSeatLayout(roomId: String) {
        roomViewModel.getRoomById(roomId)
    }

    private fun displaySeatLayout(room: Room) {
        // Build seat grid từ room layout (chỉ 8 cột, không có lối đi)
        val seatCells = SeatUtils.buildSeatGridFromRoom(room.seatLayout)
        
        // Tạo map position -> name để SeatAdapter sử dụng (A1-A8, không lối đi)
        val seatNameMap = seatCells.associate { it.position to it.name }
        
        // ✅ Hiển thị giá vé theo priceTier
        val unitPrice = PriceManager.getPrice(selectedPriceTier)
        binding.priceTxt.text = "Giá: ${PriceManager.getPriceLabel(selectedPriceTier)}/ghế"
        
        // Setup GridLayoutManager với 8 cột cố định (không aisle)
        val gridLayoutManager = GridLayoutManager(this, 8)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1
            }
        }
        
        binding.seatRecylerView.layoutManager = gridLayoutManager
        seatAdapter = SeatAdapter(
            seatCells.map { it.position },
            object : SeatAdapter.SelectedSeat {
                override fun Return(selectedName: String, num: Int) {
                    selectedSeatCount = num
                    selectedSeatPositions = seatAdapter?.getSelectedPositions() ?: emptyList()
                    binding.numberSelectedTxt.text = "$num Ghế Được Chọn"
                    // ✅ Tính giá theo priceTier
                    price = PriceManager.calculateTotal(selectedPriceTier, num)
                    binding.priceTxt.text = "Tổng: ${PriceManager.formatPrice(price)}"
                }
            },
            seatNameMap  // Truyền map A1-A8
        )
        binding.seatRecylerView.adapter = seatAdapter
        
        // ✅ Tải danh sách ghế đã được đặt từ showtime
        val selectedShowtime = showtimeViewModel.selectedShowtime.value
        if (selectedShowtime != null) {
            seatAdapter?.setUnavailableSeats(selectedShowtime.bookedSeats)
        }
    }

    // ✅ Xử lý đặt vé
    private fun bookSeats() {
        if (selectedSeatCount == 0) {
            android.widget.Toast.makeText(this, "Vui lòng chọn ghế", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedShowtime = showtimeViewModel.selectedShowtime.value
        if (selectedShowtime == null) {
            android.widget.Toast.makeText(this, "Lỗi: Không tìm thấy suất chiếu", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            android.widget.Toast.makeText(this, "Lỗi: User chưa đăng nhập", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // ✅ Lấy danh sách ghế được chọn từ adapter
        val selectedSeats = seatAdapter?.getSelectedPositions() ?: emptyList()
        if (selectedSeats.isEmpty()) {
            android.widget.Toast.makeText(this, "Danh sách ghế trống", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // ✅ Log dữ liệu đặt vé
        android.util.Log.d("SeatListActivity", "📋 Booking Data:")
        android.util.Log.d("SeatListActivity", "  - User ID: $userId")
        android.util.Log.d("SeatListActivity", "  - Showtime ID: ${selectedShowtime.id}")
        android.util.Log.d("SeatListActivity", "  - Seats: ${selectedSeats.joinToString(", ")}")
        android.util.Log.d("SeatListActivity", "  - Total Price: ${PriceManager.formatPrice(price)}")
        
        // ✅ 1. Lock ghế trước (prevent overbooking)
        showtimeViewModel.lockSeats(selectedShowtime.id, selectedSeats)
        
        // ✅ 2. Tạo Bill object
        val unitPrice = PriceManager.getPrice(selectedPriceTier)  // Giá vé đơn vị
        val bill = Bill(
            id = "",  // Sẽ được gán bởi Firestore
            userId = userId,
            showtimeId = selectedShowtime.id,
            seatPositions = selectedSeats,  // Danh sách ghế
            price = unitPrice,              // ✅ Giá vé đơn vị (tính tổng khi hiển thị)
            bookingTime = System.currentTimeMillis(),
            status = "paid",
            qrCodeData = ""  // TODO: Generate QR code
        )
        
        // ✅ 3. Thêm Bill vào Firestore
        billViewModel.addBill(bill)
        
        // ✅ 4. Book ghế (thêm vào danh sách ghế đã đặt)
        billViewModel.bookSeats(selectedShowtime.id, selectedSeats)
        
        // ✅ Hiển thị thông báo thành công
        android.widget.Toast.makeText(
            this, 
            "✅ Đã đặt ${selectedSeatCount} ghế thành công!\n${selectedSeats.joinToString(", ")}\nTổng: ${PriceManager.formatPrice(price)}", 
            android.widget.Toast.LENGTH_LONG
        ).show()
        
        // ✅ 5. Quay lại sau 2 giây (có thể thay bằng chuyển sang payment screen)
        binding.root.postDelayed({
            finish()
        }, 2000)
    }
}
