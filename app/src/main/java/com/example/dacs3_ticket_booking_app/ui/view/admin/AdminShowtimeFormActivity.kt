package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminShowtimeFormBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.TimeSlotAdapter
import com.example.dacs3_ticket_booking_app.ui.view.dialog.MovieSearchDialog
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.utils.TimeSlotManager
import java.text.SimpleDateFormat
import java.util.*

class AdminShowtimeFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminShowtimeFormBinding
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var roomViewModel: RoomViewModel

    private var editingShowtime: Showtime? = null
    private var selectedMovie: Movie? = null
    private var selectedRoom: Room? = null
    private var screeningDate: String = ""  // dd/MM/yyyy - chỉ 1 ngày
    private var selectedTimeSlot: String = ""
    private var roomList: List<Room> = emptyList()
    private var movieList: List<Movie> = emptyList()
    
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminShowtimeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)

        editingShowtime = intent.getSerializableExtra("showtime") as? Showtime

        setupButtons()
        observeViewModels()

        movieViewModel.getAllMovies()
        roomViewModel.getAllRooms()
    }

    private fun setupButtons() {
        binding.backBtn.setOnClickListener { finish() }

        // Chọn phim
        binding.cardSelectMovie.setOnClickListener {
            if (movieList.isNotEmpty()) {
                showMovieSearchDialog()
            } else {
                Toast.makeText(this, "Đang tải danh sách phim...", Toast.LENGTH_SHORT).show()
            }
        }

        // Chọn ngày chiếu
        binding.btnPickDate.setOnClickListener {
            pickScreeningDateRange()
        }

        // Room spinner change listener
        binding.spinnerRoom.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedRoom = roomList.getOrNull(position)
                    // Cập nhật danh sách khung giờ khi chọn phòng
                    updateAvailableSlots()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        )

        binding.btnSave.setOnClickListener { saveShowtime() }
    }

    private fun showMovieSearchDialog() {
        val dialog = MovieSearchDialog(
            this,
            movieList,
            onMovieSelected = { movie ->
                selectedMovie = movie
                updateSelectedMovieUI()
            }
        )
        dialog.show()
    }

    private fun updateSelectedMovieUI() {
        selectedMovie?.let { movie ->
            binding.tvSelectedMovieTitle.text = movie.title
            binding.tvSelectedMovieYear.text = movie.year.toString()
            if (movie.poster.isNotEmpty()) {
                Glide.with(this)
                    .load(movie.poster)
                    .into(binding.ivSelectedMoviePoster)
            }
        }
    }

     private fun pickScreeningDateRange() {
         val cal = Calendar.getInstance()
         val year = cal.get(Calendar.YEAR)
         val month = cal.get(Calendar.MONTH)
         val day = cal.get(Calendar.DAY_OF_MONTH)

         // Chọn ngày chiếu (chỉ 1 ngày, không phải range)
         DatePickerDialog(this, { _, y, m, d ->
             val selectedCal = Calendar.getInstance()
             selectedCal.set(y, m, d)
             
             // Kiểm tra xem ngày được chọn có phải ngày quá khứ không
             val selectedDate = selectedCal.timeInMillis
             val today = Calendar.getInstance()
             today.set(Calendar.HOUR_OF_DAY, 0)
             today.set(Calendar.MINUTE, 0)
             today.set(Calendar.SECOND, 0)
             today.set(Calendar.MILLISECOND, 0)
             
             if (selectedDate < today.timeInMillis) {
                 Toast.makeText(this, "Không thể chọn ngày quá khứ", Toast.LENGTH_SHORT).show()
                 return@DatePickerDialog
             }
             
             screeningDate = sdfDisplay.format(selectedCal.time)
             binding.tvScreeningDate.text = "📅 $screeningDate"
             selectedTimeSlot = ""
             updateAvailableSlots()
         }, year, month, day).show()
     }

     private fun updateAvailableSlots() {
         if (screeningDate.isEmpty() || selectedRoom == null) {
             binding.rvTimeSlots.adapter = TimeSlotAdapter(emptyList()) {}
             return
         }

         // Kiểm tra khung giờ trống cho ngày chiếu
         showtimeViewModel.getShowtimesByRoomAndDate(selectedRoom!!.id, screeningDate) { existingShowtimes ->
             val availableSlots = TimeSlotManager.getAvailableSlots(
                 screeningDate,
                 selectedRoom!!.id,
                 existingShowtimes
             )

             timeSlotAdapter = TimeSlotAdapter(
                 slots = availableSlots,
                 onSlotSelected = { slot ->
                     selectedTimeSlot = slot
                     val priceTier = TimeSlotManager.getPriceTierFromSlot(slot)
                     binding.tvPriceTier.text = "Khung giờ: ${slot} - ${getPriceTierLabel(priceTier)}"
                 }
             )
             binding.rvTimeSlots.adapter = timeSlotAdapter
             binding.rvTimeSlots.layoutManager = GridLayoutManager(this, 2)
         }
     }

    private fun getPriceTierLabel(tier: String): String = when (tier) {
        "morning" -> "Sáng"
        "afternoon" -> "Chiều"
        "evening" -> "Tối"
        else -> tier
    }

     private fun observeViewModels() {
         movieViewModel.movies.observe(this) { movies ->
             movieList = movies
             editingShowtime?.let { st ->
                 selectedMovie = movies.find { it.id == st.movieId }
                 updateSelectedMovieUI()
             }
         }

          roomViewModel.rooms.observe(this) { rooms ->
              roomList = rooms
              val names = rooms.map { it.name }
              // Custom adapter với màu chữ trắng
              val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {
                  override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                      val view = super.getView(position, convertView, parent)
                      (view as? android.widget.TextView)?.setTextColor(android.graphics.Color.WHITE)
                      return view
                  }

                  override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                      val view = super.getDropDownView(position, convertView, parent)
                      (view as? android.widget.TextView)?.setTextColor(android.graphics.Color.WHITE)
                      return view
                  }
              }
              adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
              binding.spinnerRoom.adapter = adapter

              // Set selection if editing
              editingShowtime?.let { st ->
                  val idx = rooms.indexOfFirst { it.id == st.roomId }
                  if (idx >= 0) binding.spinnerRoom.setSelection(idx)
              }
          }

         // Populate if editing
         editingShowtime?.let { st ->
             screeningDate = st.screeningDate
             selectedTimeSlot = st.timeSlot
             binding.tvScreeningDate.text = "${st.screeningDate}"
             binding.tvPriceTier.text = "Khung giờ: ${st.timeSlot} - ${getPriceTierLabel(st.priceTier)}"
             binding.titleBar.text = "Sửa suất chiếu"
         } ?: run {
             binding.titleBar.text = "Thêm suất chiếu"
             // Hiển thị gợi ý ngày hôm nay
             val todayStr = sdfDisplay.format(Date())
             binding.tvScreeningDate.text = "(Hôm nay: $todayStr)"
         }

         showtimeViewModel.successMessage.observe(this) { msg ->
             Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
             finish()
         }

         showtimeViewModel.errorMessage.observe(this) { msg ->
             Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
         }
     }

    private fun saveShowtime() {
        if (selectedMovie == null) {
            Toast.makeText(this, "Vui lòng chọn phim", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedRoom == null) {
            Toast.makeText(this, "Vui lòng chọn phòng chiếu", Toast.LENGTH_SHORT).show()
            return
        }
        if (screeningDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày chiếu", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTimeSlot.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn khung giờ", Toast.LENGTH_SHORT).show()
            return
        }

        val priceTier = TimeSlotManager.getPriceTierFromSlot(selectedTimeSlot)
        
        val showtime = if (editingShowtime != null) {
            // Update: giữ ID và dữ liệu ghế cũ
            editingShowtime!!.copy(
                movieId = selectedMovie!!.id,
                roomId = selectedRoom!!.id,
                screeningDate = screeningDate,
                timeSlot = selectedTimeSlot,
                priceTier = priceTier
            )
        } else {
            // Tạo mới
            Showtime(
                movieId = selectedMovie!!.id,
                roomId = selectedRoom!!.id,
                screeningDate = screeningDate,
                timeSlot = selectedTimeSlot,
                priceTier = priceTier
            )
        }
        
        if (editingShowtime != null) {
            showtimeViewModel.updateShowtime(showtime)
        } else {
            showtimeViewModel.addShowtime(showtime)
        }
    }
}
