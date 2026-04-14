package com.example.dacs3_ticket_booking_app.ui.view.admin

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
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminShowtimeDetailBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.utils.SeatUtils
import java.text.SimpleDateFormat
import java.util.*

class AdminShowtimeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminShowtimeDetailBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var roomViewModel: RoomViewModel
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminShowtimeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val showtime = intent.getSerializableExtra("showtime") as? Showtime
        if (showtime == null) {
            Toast.makeText(this, "Không tìm thấy suất chiếu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.progressBar.visibility = View.VISIBLE

        // Display time info
        binding.tvStartTime.text = "Ngày: ${showtime.screeningDate}"
        binding.tvEndTime.text = "Giờ: ${showtime.timeSlot}"
        binding.tvPriceTier.text = SeatUtils.priceTierLabel(showtime.priceTier)
        binding.tvBookedCount.text = "Đã đặt: ${showtime.bookedSeats.size} ghế"
        binding.tvBookedSeatsDetail.text = if (showtime.bookedSeats.isNotEmpty())
            SeatUtils.positionsToDisplay(showtime.bookedSeats) else "Chưa có ghế nào được đặt"

        // Load movie title
        movieViewModel.getAllMovies()
        movieViewModel.movies.observe(this) { movies ->
            val movie = movies.find { it.id == showtime.movieId }
            binding.tvMovieName.text = movie?.title ?: "Không rõ"
        }

        // Load room and build seat map
        roomViewModel.getRoomById(showtime.roomId)
        roomViewModel.roomDetail.observe(this) { room ->
            room?.let {
                binding.tvRoomName.text = it.name

                val totalSeats = it.seatLayout.sumOf { row -> row.count { c -> c == '1' } }
                val available = totalSeats - showtime.bookedSeats.size
                binding.tvAvailableCount.text = "Còn trống: $available ghế"

                binding.progressBar.visibility = View.GONE
                buildSeatMap(it, showtime.bookedSeats)
            }
        }
    }

    private fun buildSeatMap(room: Room, bookedSeats: List<String>) {
        binding.seatMapContainer.removeAllViews()
        val seatSizeDp = 24
        val scale = resources.displayMetrics.density
        val seatSizePx = (seatSizeDp * scale).toInt()
        val marginPx = (2 * scale).toInt()

        // Build header với cột 1-8
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.HORIZONTAL
        headerLayout.gravity = Gravity.CENTER
        
        // Empty space cho row label
        val emptySpace = TextView(this)
        emptySpace.layoutParams = LinearLayout.LayoutParams(seatSizePx, seatSizePx)
        (emptySpace.layoutParams as LinearLayout.LayoutParams).setMargins(0, marginPx, 4, marginPx)
        headerLayout.addView(emptySpace)
        
        // Column headers 1-8
        repeat(8) { colIdx ->
            val colLabel = TextView(this)
            colLabel.text = (colIdx + 1).toString()
            colLabel.setTextColor(Color.parseColor("#888888"))
            colLabel.textSize = 8f
            colLabel.gravity = Gravity.CENTER
            colLabel.layoutParams = LinearLayout.LayoutParams(seatSizePx, seatSizePx)
            (colLabel.layoutParams as LinearLayout.LayoutParams).setMargins(marginPx, marginPx, marginPx, marginPx)
            headerLayout.addView(colLabel)
        }
        binding.seatMapContainer.addView(headerLayout)

        // Build seat rows (chỉ hiển thị '1', bỏ '0' hoàn toàn)
        room.seatLayout.forEachIndexed { rowIdx, rowStr ->
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER

            // Row label A, B, C...
            val rowLabel = TextView(this)
            rowLabel.text = ('A' + rowIdx).toString()
            rowLabel.setTextColor(Color.parseColor("#888888"))
            rowLabel.textSize = 10f
            rowLabel.gravity = Gravity.CENTER
            val labelParams = LinearLayout.LayoutParams(seatSizePx, seatSizePx)
            labelParams.setMargins(0, marginPx, 4, marginPx)
            rowLabel.layoutParams = labelParams
            rowLayout.addView(rowLabel)

            // Build seats - chỉ thêm những cái '1'
            var seatColIdx = 0  // Đếm cột ghế thực (0-7)
            rowStr.forEachIndexed { charIdx, cell ->
                if (cell == '1') {
                    seatColIdx++
                    val seatView = TextView(this)
                    val params = LinearLayout.LayoutParams(seatSizePx, seatSizePx)
                    params.setMargins(marginPx, marginPx, marginPx, marginPx)
                    seatView.layoutParams = params
                    seatView.gravity = Gravity.CENTER
                    seatView.textSize = 8f

                    val position = "${rowIdx}_${charIdx}"
                    val seatName = "${('A' + rowIdx).toString()}${seatColIdx}"
                    
                    when {
                        bookedSeats.contains(position) -> {
                            // Booked seat
                            seatView.setBackgroundColor(Color.parseColor("#FF5252"))
                            seatView.text = seatName
                            seatView.setTextColor(Color.WHITE)
                        }
                        else -> {
                            // Available seat
                            seatView.setBackgroundColor(Color.parseColor("#4CAF50"))
                            seatView.text = seatName
                            seatView.setTextColor(Color.WHITE)
                        }
                    }
                    rowLayout.addView(seatView)
                }
            }

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowParams.bottomMargin = marginPx
            rowLayout.layoutParams = rowParams
            binding.seatMapContainer.addView(rowLayout)
        }
    }
}
