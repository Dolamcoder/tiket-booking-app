package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminRoomDetailBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminRoomDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRoomDetailBinding
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var movieViewModel: MovieViewModel
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var currentRoom: Room? = null
    // Map movieId -> movieTitle for display
    private val movieTitleMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        val room = intent.getSerializableExtra("room") as? Room
        if (room == null) {
            Toast.makeText(this, "Không tìm thấy phòng chiếu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentRoom = room

        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AdminRoomFormActivity::class.java)
            intent.putExtra("room", room)
            startActivity(intent)
        }

        displayRoomInfo(room)
        buildSeatMap(room)

        // Load movies map then showtimes
        movieViewModel.getAllMovies()
        movieViewModel.movies.observe(this) { movies ->
            movies.forEach { movieTitleMap[it.id] = it.title }
            // Load all showtimes for debugging
            showtimeViewModel.getAllShowtimes()
        }

        showtimeViewModel.showtimes.observe(this) { allShowtimes ->
            // Filter chỉ lấy suất chiếu của phòng này
            val roomShowtimes = allShowtimes.filter { it.roomId == currentRoom?.id }
            // Sort theo screeningDate và timeSlot
            val sorted = roomShowtimes.sortedWith(compareBy<Showtime> { it.screeningDate }.thenBy { it.timeSlot })
            Toast.makeText(this, "Tổng: ${allShowtimes.size} suất, phòng này: ${sorted.size}", Toast.LENGTH_SHORT).show()
            displayUpcomingShowtimes(sorted)
        }
        
        showtimeViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayRoomInfo(room: Room) {
        binding.tvRoomName.text = room.name
        binding.tvRowCount.text = room.rowCount.toString()
        binding.tvColCount.text = room.colCount.toString()
        val totalSeats = room.seatLayout.sumOf { row -> row.count { it == '1' } }
        binding.tvTotalSeats.text = totalSeats.toString()
    }

    private fun buildSeatMap(room: Room) {
        binding.seatMapContainer.removeAllViews()
        val seatSizeDp = 22
        val scale = resources.displayMetrics.density

        // Add column header with numbers
        if (room.seatLayout.isNotEmpty()) {
            val headerLayout = LinearLayout(this)
            headerLayout.orientation = LinearLayout.HORIZONTAL
            headerLayout.gravity = Gravity.CENTER

            // Empty space for row label
            val emptySpace = View(this)
            val emptyParams = LinearLayout.LayoutParams(
                (seatSizeDp * scale).toInt(),
                (seatSizeDp * scale).toInt()
            )
            emptyParams.setMargins(0, 2, 4, 2)
            emptySpace.layoutParams = emptyParams
            headerLayout.addView(emptySpace)

            // Add column numbers
            room.seatLayout[0].forEachIndexed { colIdx, _ ->
                val colLabel = TextView(this)
                colLabel.text = (colIdx + 1).toString()
                colLabel.setTextColor(Color.parseColor("#888888"))
                colLabel.textSize = 9f
                val colParams = LinearLayout.LayoutParams(
                    (seatSizeDp * scale).toInt(),
                    (seatSizeDp * scale).toInt()
                )
                colParams.setMargins(2, 2, 2, 2)
                colLabel.layoutParams = colParams
                colLabel.gravity = Gravity.CENTER
                headerLayout.addView(colLabel)
            }

            val headerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            headerParams.bottomMargin = 8
            headerLayout.layoutParams = headerParams
            binding.seatMapContainer.addView(headerLayout)
        }

        room.seatLayout.forEachIndexed { rowIdx, rowStr ->
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER

            // Row label
            val rowLabel = TextView(this)
            rowLabel.text = ('A' + rowIdx).toString()
            rowLabel.setTextColor(Color.parseColor("#888888"))
            rowLabel.textSize = 10f
            val labelParams = LinearLayout.LayoutParams(
                (seatSizeDp * scale).toInt(),
                (seatSizeDp * scale).toInt()
            )
            labelParams.setMargins(0, 2, 4, 2)
            rowLabel.layoutParams = labelParams
            rowLabel.gravity = Gravity.CENTER
            rowLayout.addView(rowLabel)

            rowStr.forEachIndexed { colIdx, cell ->
                val seatView = View(this)
                val params = LinearLayout.LayoutParams(
                    (seatSizeDp * scale).toInt(),
                    (seatSizeDp * scale).toInt()
                )
                params.setMargins(2, 2, 2, 2)
                seatView.layoutParams = params

                if (cell == '1') {
                    seatView.setBackgroundColor(Color.parseColor("#4CAF50"))
                } else {
                    seatView.setBackgroundColor(Color.parseColor("#1E2240"))
                }
                rowLayout.addView(seatView)
            }

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowParams.bottomMargin = 3
            rowLayout.layoutParams = rowParams
            binding.seatMapContainer.addView(rowLayout)
        }
    }

    private fun displayUpcomingShowtimes(showtimes: List<Showtime>) {
        binding.showtimeListContainer.removeAllViews()
        binding.tvShowtimeCount.text = "${showtimes.size} suất"

        if (showtimes.isEmpty()) {
            binding.tvNoShowtime.visibility = View.VISIBLE
            binding.tvNoShowtime.text = "Chưa có suất chiếu nào"
            return
        }
        binding.tvNoShowtime.visibility = View.GONE

        showtimes.forEachIndexed { idx, showtime ->
            val card = layoutInflater.inflate(
                android.R.layout.preference_category,
                binding.showtimeListContainer,
                false
            )
            // Create a custom card view
            val itemLayout = LinearLayout(this)
            itemLayout.orientation = LinearLayout.VERTICAL
            itemLayout.setBackgroundColor(Color.parseColor("#16223B"))
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.bottomMargin = (8 * resources.displayMetrics.density).toInt()
            itemLayout.layoutParams = cardParams
            itemLayout.setPadding(
                (12 * resources.displayMetrics.density).toInt(),
                (10 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt(),
                (10 * resources.displayMetrics.density).toInt()
            )

            // Add click listener to navigate to detail
            itemLayout.setOnClickListener {
                val intent = Intent(this, AdminShowtimeDetailActivity::class.java)
                intent.putExtra("showtime", showtime)
                startActivity(intent)
            }

            // Make it look clickable
            itemLayout.isClickable = true
            itemLayout.isFocusable = true
            itemLayout.foreground = android.content.res.Resources.getSystem().getDrawable(android.R.drawable.list_selector_background, null)

            val movieTitle = movieTitleMap[showtime.movieId] ?: showtime.movieId

            val tvMovie = TextView(this)
            tvMovie.text = movieTitle
            tvMovie.setTextColor(Color.WHITE)
            tvMovie.textSize = 14f
            tvMovie.typeface = android.graphics.Typeface.DEFAULT_BOLD

            val tvTime = TextView(this)
            tvTime.text = "Ngày: ${showtime.screeningDate} | Giờ: ${showtime.timeSlot}"
            tvTime.setTextColor(Color.parseColor("#94A3B8"))
            tvTime.textSize = 12f

            val tvBooked = TextView(this)
            tvBooked.text = "${showtime.bookedSeats.size} ghế đã đặt"
            tvBooked.setTextColor(Color.parseColor("#F59E0B"))
            tvBooked.textSize = 12f

            itemLayout.addView(tvMovie)
            itemLayout.addView(tvTime)
            itemLayout.addView(tvBooked)
            binding.showtimeListContainer.addView(itemLayout)
        }
    }
}
