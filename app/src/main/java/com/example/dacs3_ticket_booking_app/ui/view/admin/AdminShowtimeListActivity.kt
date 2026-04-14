package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.data.model.Showtime
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminShowtimeListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminShowtimeAdapter
import com.example.dacs3_ticket_booking_app.ui.view.dialog.MovieSearchDialog
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.ShowtimeViewModel
import com.example.dacs3_ticket_booking_app.utils.TimeSlotManager

class AdminShowtimeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminShowtimeListBinding
    private lateinit var showtimeViewModel: ShowtimeViewModel
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var roomViewModel: RoomViewModel

    private val movieTitleMap = mutableMapOf<String, String>()
    private val roomNameMap = mutableMapOf<String, String>()
    private var moviesLoaded = false
    private var roomsLoaded = false
    
    // Filter state
    private var allShowtimes = listOf<Showtime>()
    private var selectedMovieId: String = ""
    private var selectedTimeSlot: String = ""
    private var movieList = listOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminShowtimeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showtimeViewModel = ViewModelProvider(this).get(ShowtimeViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.fabAddShowtime.setOnClickListener {
            startActivity(Intent(this, AdminShowtimeFormActivity::class.java))
        }

        // Chọn film
        binding.btnSelectMovie.setOnClickListener {
            if (movieList.isNotEmpty()) {
                showMovieSearchDialog()
            }
        }

        // Chọn khung giờ
        val timeSlots = mutableListOf("Tất cả khung giờ")
        timeSlots.addAll(TimeSlotManager.getAllSlots())
        val adapter = object : android.widget.ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeSlots) {
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
        binding.spinnerTimeSlot.adapter = adapter

        binding.spinnerTimeSlot.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedTimeSlot = if (position == 0) "" else TimeSlotManager.getAllSlots()[position - 1]
                applyFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Xóa lọc
        binding.btnClearFilter.setOnClickListener {
            selectedMovieId = ""
            selectedTimeSlot = ""
            binding.spinnerTimeSlot.setSelection(0)
            applyFilters()
        }

        binding.recyclerViewShowtimes.layoutManager = LinearLayoutManager(this)
        setupObservers()

        // Load all data
        movieViewModel.getAllMovies()
        roomViewModel.getAllRooms()
    }

    override fun onResume() {
        super.onResume()
        showtimeViewModel.getAllShowtimes()
    }

    private fun setupObservers() {
        movieViewModel.movies.observe(this) { movies ->
            movieList = movies
            movies.forEach { movieTitleMap[it.id] = it.title }
            moviesLoaded = true
            if (moviesLoaded && roomsLoaded) showtimeViewModel.getAllShowtimes()
        }

        roomViewModel.rooms.observe(this) { rooms ->
            rooms.forEach { roomNameMap[it.id] = it.name }
            roomsLoaded = true
            if (moviesLoaded && roomsLoaded) showtimeViewModel.getAllShowtimes()
        }

        showtimeViewModel.showtimes.observe(this) { showtimes ->
            allShowtimes = showtimes
            applyFilters()
        }

        showtimeViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        showtimeViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        showtimeViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMovieSearchDialog() {
        val dialog = MovieSearchDialog(
            this,
            movieList,
            onMovieSelected = { movie ->
                selectedMovieId = movie.id

                applyFilters()
            }
        )
        dialog.show()
    }

     private fun applyFilters() {
         var filtered = allShowtimes

         if (selectedMovieId.isNotEmpty()) {
             filtered = filtered.filter { it.movieId == selectedMovieId }
         }

         if (selectedTimeSlot.isNotEmpty()) {
             filtered = filtered.filter { it.timeSlot == selectedTimeSlot }
         }

         // Hiển thị thông báo nếu không có kết quả
         if (filtered.isEmpty()) {
             binding.recyclerViewShowtimes.adapter = null
         } else {
             binding.recyclerViewShowtimes.adapter = AdminShowtimeAdapter(
                 filtered.toMutableList(),
                 onEdit = { showtime ->
                     val intent = Intent(this, AdminShowtimeFormActivity::class.java)
                     intent.putExtra("showtime", showtime)
                     startActivity(intent)
                 },
                 onDelete = { showtime -> showtimeViewModel.deleteShowtime(showtime.id) },
                 onDetail = { showtime ->
                     val intent = Intent(this, AdminShowtimeDetailActivity::class.java)
                     intent.putExtra("showtime", showtime)
                     startActivity(intent)
                 },
                 movieTitleMap = movieTitleMap,
                 roomNameMap = roomNameMap
             )
         }
     }
}
