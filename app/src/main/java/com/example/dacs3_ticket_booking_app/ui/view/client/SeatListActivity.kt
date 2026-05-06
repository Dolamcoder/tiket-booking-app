package com.example.dacs3_ticket_booking_app.ui.view.client

import android.content.Intent
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
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.QRViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.utils.PriceManager
import com.example.dacs3_ticket_booking_app.utils.QRUtils
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import com.google.firebase.auth.FirebaseAuth

class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var movie: Movie
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var billViewModel: BillViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var qrViewModel: QRViewModel
    
    private var selectedScreeningDate: String = ""
    private var selectedTimeSlot: String = ""
    private var selectedRoom: Room? = null
    private var selectedPriceTier: String = "morning"
    private var price: Double = 0.0
    private var selectedSeatCount: Int = 0
    private var selectedSeatPositions: List<String> = emptyList()
    private var seatAdapter: SeatAdapter? = null
    private var pendingBillId: String = ""  // Store billId khi đang chờ QR generate
    private var showtimeListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var lockedPositions: MutableSet<String> = mutableSetOf()
    private var billSuccessObserverAdded = false
    private var qrObserverAdded = false

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

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Remove listener khi activity bị destroy
        showtimeListenerRegistration?.remove()
        releaseAllLockedSeats()
    }

    private fun getIntentExtra() {
        movie = intent.getSerializableExtra("movie") as Movie
    }

    private fun setupViewModels() {
        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        firebaseAuth = FirebaseAuth.getInstance()
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        qrViewModel = ViewModelProvider(this).get(QRViewModel::class.java)
    }

    private fun backOnClick() {
        binding.backBtn.setOnClickListener { 
            // ✅ Release tất cả lock khi user quay lại
            releaseAllLockedSeats()
            finish() 
        }
        binding.btnBooking.setOnClickListener {
            if (selectedSeatCount == 0) {
                android.widget.Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmBooking()
        }
    }
    
    override fun onBackPressed() {
        releaseAllLockedSeats()
        super.onBackPressed()
    }
    
    private fun releaseAllLockedSeats() {
        if (lockedPositions.isNotEmpty()) {
            val selectedShowtime = showtimeViewModel.selectedShowtime.value
            if (selectedShowtime != null) {
                for (pos in lockedPositions) {
                    showtimeViewModel.releaseLockedSeats(selectedShowtime.id, listOf(pos))
                }
                lockedPositions.clear()
            }
        }
    }
    
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
            if (showtime != null) {
                loadSeatLayout(showtime.roomId)
            }
        }

        // Theo dõi phòng chiếu
        roomViewModel.roomDetail.observe(this) { room ->
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
            android.util.Log.e("SeatListActivity", "❌ Showtime Error: $msg")
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
        }

        // Theo dõi lỗi từ Bill ViewModel
        billViewModel.errorMessage.observe(this) { msg ->
            android.util.Log.e("SeatListActivity", "❌ Bill Error: $msg")
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
        }

        // ✅ Observer thành công tạo Bill - chỉ thêm một lần
        if (!billSuccessObserverAdded) {
            billViewModel.successMessage.observe(this) { msg ->
                if (msg.contains("successfully")) {
                    // Extract bill ID từ message "Bill created successfully (ID: xyz)"
                    val regex = "\\(ID: ([^)]+)\\)".toRegex()
                    val matchResult = regex.find(msg)
                    val billId = matchResult?.groupValues?.get(1) ?: ""
                    if (billId.isNotEmpty()) {
                        pendingBillId = billId
                        val selectedShowtime = showtimeViewModel.selectedShowtime.value
                        if (selectedShowtime != null) {
                            val endTime = QRUtils.calculateEndTime(selectedScreeningDate, selectedTimeSlot)
                            android.util.Log.d("SeatListActivity", "📱 Calling API to generate QR: billId=$billId, endTime=$endTime")
                            qrViewModel.generateQR(billId, endTime)
                        }
                    }
                }
            }
            billSuccessObserverAdded = true
        }

        // ✅ Observer QR response - chỉ thêm một lần
        if (!qrObserverAdded) {
            qrViewModel.qrCodeData.observe(this) { qrResponse ->
                if (qrResponse != null && qrResponse.success) {
                    android.util.Log.d("SeatListActivity", "✅ QR generated successfully")
                    // ✅ Navigate to Payment Activity
                    billViewModel.updateBillQRData(pendingBillId, qrResponse.qrImage?:"")
                    val paymentIntent = Intent(this@SeatListActivity, PaymentActivity::class.java).apply {
                        putExtra(PaymentActivity.BILL_ID, pendingBillId)
                        putExtra(PaymentActivity.AMOUNT, (price * selectedSeatCount).toLong())
                        putExtra(PaymentActivity.SHOWTIME_ID, showtimeViewModel.selectedShowtime.value?.id ?: "")
                        putStringArrayListExtra(PaymentActivity.SELECTED_SEATS, ArrayList(selectedSeatPositions))
                        putExtra("qrImageData", qrResponse.qrImage ?: "")
                        putExtra("qrSignature", qrResponse.qrData?.signature ?: "")
                    }
                    startActivityForResult(paymentIntent, PAYMENT_REQUEST_CODE)
                }
            }
            qrObserverAdded = true
        }

        qrViewModel.errorMessage.observe(this) { msg ->
            android.util.Log.e("SeatListActivity", "❌ QR Error: $msg")
            android.widget.Toast.makeText(this, "Lỗi tạo QR: $msg", android.widget.Toast.LENGTH_SHORT).show()
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
        
        android.util.Log.d("SeatListActivity", "📍 Seat map size: ${seatNameMap.size}")
        
        // ✅ Hiển thị giá vé theo priceTier
        val unitPrice = PriceManager.getPrice(selectedPriceTier)
        binding.priceTxt.text = "Giá: ${PriceManager.getPriceLabel(selectedPriceTier)}/ghế"
        
        val gridLayoutManager = GridLayoutManager(this, 8)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = 1
        }
        
        binding.seatRecylerView.layoutManager = gridLayoutManager
        seatAdapter = SeatAdapter(
            seatCells.map { it.position },
            object : SeatAdapter.SelectedSeat {
                override fun Return(selectedName: String, num: Int) {
                    selectedSeatCount = num
                    selectedSeatPositions = seatAdapter?.getSelectedPositions() ?: emptyList()
                    android.util.Log.d("SeatListActivity", "✅ Selected positions: $selectedSeatPositions, count: $num")
                    binding.numberSelectedTxt.text = "$num Ghế Được Chọn"
                    price = PriceManager.calculateTotal(selectedPriceTier, num)
                    binding.priceTxt.text = "Tổng: ${PriceManager.formatPrice(price)}"
                    
                    // ✅ LOCK GHẾ NGAY KHI CHỌN
                    lockSelectedSeats()
                }
            },
            seatNameMap
        )
        binding.seatRecylerView.adapter = seatAdapter
        
        // ✅ Tải danh sách ghế đã được đặt từ showtime
        val selectedShowtime = showtimeViewModel.selectedShowtime.value
        if (selectedShowtime != null) {
            android.util.Log.d("SeatListActivity", "🔒 Setting unavailable seats: ${selectedShowtime.bookedSeats}, locked: ${selectedShowtime.lockedSeats.keys}")
            // ✅ Hiển thị ghế lock của người khác + ghế đã booked
            // Không bao gồm ghế lock của chính mình (lockedPositions trống lúc đầu)
            seatAdapter?.setUnavailableSeats((selectedShowtime.bookedSeats + selectedShowtime.lockedSeats.keys).toList())
            
            // ✅ Real-time listener để cập nhật ghế
            setupShowtimeListener(selectedShowtime.id)
        }
    }
    
    // ✅ LOCK/UNLOCK GHẾ KHI CHỌN/BỎ CHỌN
    private fun lockSelectedSeats() {
        val selectedShowtime = showtimeViewModel.selectedShowtime.value
        if (selectedShowtime == null) return
        
        // Tìm ghế được chọn hiện tại
        val currentSelected = selectedSeatPositions.toSet()
        
        // Ghế mới được chọn (chưa lock)
        val newlySelected = currentSelected - lockedPositions
        
        // Ghế bị bỏ chọn (cần unlock)
        val deselected = lockedPositions - currentSelected
        
        // Lock ghế mới được chọn
        if (newlySelected.isNotEmpty()) {
            showtimeViewModel.lockSeats(selectedShowtime.id, newlySelected.toList())
            lockedPositions.addAll(newlySelected)
        }
        
        // Unlock ghế bị bỏ chọn
        if (deselected.isNotEmpty()) {
            for (pos in deselected) {
                showtimeViewModel.releaseLockedSeats(selectedShowtime.id, listOf(pos))
            }
            lockedPositions.removeAll(deselected)
        }
    }
    
    // ✅ Real-time listener
    private fun setupShowtimeListener(showtimeId: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val showtimeRef = db.collection("showtimes").document(showtimeId)
        
        showtimeListenerRegistration?.remove()
        
        showtimeListenerRegistration = showtimeRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("SeatListActivity", "❌ Listener error: ${error.message}")
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                val updatedShowtime = snapshot.toObject(com.example.dacs3_ticket_booking_app.data.model.Showtime::class.java)
                if (updatedShowtime != null) {
                    // ✅ Không bao gồm ghế lock của chính mình (lockedPositions)
                    // Chỉ hiển thị ghế lock của người khác
                    val othersLockedSeats = (updatedShowtime.lockedSeats.keys - lockedPositions).toList()
                    val allUnavailable = updatedShowtime.bookedSeats + othersLockedSeats
                    android.util.Log.d("SeatListActivity", "🔄 Updated unavailable seats: $allUnavailable, my locked: $lockedPositions")
                    seatAdapter?.updateSeatsStatus(allUnavailable)
                }
            }
        }
    }
    
    // ✅ THANH TOÁN - tạo Bill rồi redirect sang payment
    private fun confirmBooking() {
        if (selectedSeatCount == 0) {
            android.widget.Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", android.widget.Toast.LENGTH_SHORT).show()
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
        
        val selectedSeats = seatAdapter?.getSelectedPositions() ?: emptyList()
        if (selectedSeats.isEmpty()) {
            android.widget.Toast.makeText(this, "Danh sách ghế trống", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("SeatListActivity", "💾 Creating bill with ${selectedSeats.size} seats: $selectedSeats")
        
        val unitPrice = PriceManager.getPrice(selectedPriceTier)
        val bill = Bill(
            id = "",
            userId = userId,
            showtimeId = selectedShowtime.id,
            seatPositions = selectedSeats,
            price = unitPrice,
            bookingTime = System.currentTimeMillis(),
            status = "pending",
            qrCodeData = ""
        )

        // ✅ Tạo Bill trong Firestore (nhận ID)
        // Observer sẽ tự động trigger khi addBill() thành công
        billViewModel.addBill(bill)
    }

    // ✅ Handle Payment Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // ✅ Payment thành công - Bill status & Showtime đã được update ở PaymentActivity
                    // Chỉ cần update revenue
                    movieViewModel.updateRevenue(movie.id, price * selectedSeatCount, selectedSeatCount)
                    lockedPositions.clear()
                    android.widget.Toast.makeText(
                        this,
                        "Đã đặt vé thành công",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    // ✅ Delay một chút rồi quay lại
                    binding.root.postDelayed({
                        finish()
                    }, 1500)
                }
                RESULT_CANCELED -> {
                    // ❌ Payment thất bại - Bill & ghế đã được xóa/unlock ở PaymentActivity
                    lockedPositions.clear()
                    android.widget.Toast.makeText(
                        this,
                        "Thanh toán thất bại. Vui lòng thử lại",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val PAYMENT_REQUEST_CODE = 100
    }
}
