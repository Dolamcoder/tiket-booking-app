package com.example.dacs3_ticket_booking_app.ui.view.client

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ActivitySearchMovieBinding
import com.example.dacs3_ticket_booking_app.ui.view.adaper.MovieAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.GenreViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.google.android.material.chip.Chip

class SearchMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchMovieBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var genreViewModel: GenreViewModel
    private lateinit var movieAdapter: MovieAdapter

    // Filter variables
    private var selectedGenres = mutableSetOf<String>()
    private val timeSlotsData = listOf(
        "Tất cả khung giờ",
        "Sáng (08:00-12:00)",
        "Chiều (12:00-18:00)",
        "Tối (18:00+)"
    )
    private val timeSlotsMap = mapOf(
        "Tất cả khung giờ" to null,
        "Sáng (08:00-12:00)" to "morning",
        "Chiều (12:00-18:00)" to "afternoon",
        "Tối (18:00+)" to "evening"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        // Initialize ViewModels
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        genreViewModel = ViewModelProvider(this).get(GenreViewModel::class.java)

        // Setup UI
        setupUI()
        setupRecyclerView()
        observeViewModels()

        // Load initial data
        movieViewModel.getAllMovies()
        genreViewModel.getAllGenres()
        
        // ✅ Auto-display all movies when entering the page
        binding.recyclerViewResults.post {
            Handler(Looper.getMainLooper()).postDelayed({
                val allMovies = movieViewModel.movies.value ?: emptyList()
                if (allMovies.isNotEmpty()) {
                    updateMovieResults(allMovies)
                }
            }, 500) // Wait 500ms for movies to be loaded
        }
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Search button
        binding.btnSearch.setOnClickListener {
            performAdvancedSearch()
        }

        // Clear button
        binding.btnClear.setOnClickListener {
            clearFilters()
        }

        // Setup time slot spinner
        setupTimeSlotSpinner()

        // Real-time search on title change
        binding.edtTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                triggerRealtimeSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Real-time search on description change
        binding.edtDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                triggerRealtimeSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Real-time search on cast name change
        binding.edtCastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                triggerRealtimeSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(mutableListOf(), null)

        binding.recyclerViewResults.apply {
            layoutManager = GridLayoutManager(this@SearchMovieActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun setupTimeSlotSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            timeSlotsData
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeSlot.adapter = adapter

        binding.spinnerTimeSlot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Trigger real-time search when time slot changes
                triggerRealtimeSearch()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModels() {
        // Observe movies
        movieViewModel.searchResults.observe(this) { results ->
            updateMovieResults(results)
        }

        // Observe all movies for fallback
        movieViewModel.movies.observe(this) { movies ->
            if (movies.isNotEmpty()) {
                updateMovieResults(movies)
            }
        }

        // Observe genres for chip group
        genreViewModel.genres.observe(this) { genres ->
            setupGenreChips(genres)
        }

        // Observe loading state
        movieViewModel.isLoading.observe(this) { isLoading ->
            binding.progressSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        movieViewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGenreChips(genres: List<Genre>) {
        binding.chipGroupGenres.removeAllViews()

        genres.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre.name
                isClickable = true
                isCheckable = true
                tag = genre.id

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedGenres.add(genre.id)
                    } else {
                        selectedGenres.remove(genre.id)
                    }
                    // Trigger real-time search when genre selection changes
                    triggerRealtimeSearch()
                }
            }
            binding.chipGroupGenres.addView(chip)
        }
    }

    private fun performAdvancedSearch() {
        val title = binding.edtTitle.text.toString().ifEmpty { null }
        val description = binding.edtDescription.text.toString().ifEmpty { null }
        val castName = binding.edtCastName.text.toString().ifEmpty { null }

        // Get selected time slot
        val selectedTimeSlot = binding.spinnerTimeSlot.selectedItem as String
        val priceTier = timeSlotsMap[selectedTimeSlot]

        // Prepare genres list
        val genresList = selectedGenres.ifEmpty { null }?.toList()

        // Show loading
        binding.progressSearch.visibility = View.VISIBLE

        // Perform search
        movieViewModel.advancedSearch(
            title = title,
            description = description,
            genres = genresList,
            castName = castName,
            priceTier = priceTier
        )
    }

    // 🔄 Real-time search khi thay đổi filters
    private fun triggerRealtimeSearch() {
        performAdvancedSearch()
    }

    private fun updateMovieResults(movies: List<Movie>) {
        binding.progressSearch.visibility = View.GONE

        if (movies.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewResults.visibility = View.GONE
            binding.tvResultCount.text = getString(R.string.zero_movies)
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewResults.visibility = View.VISIBLE
            binding.tvResultCount.text = getString(R.string.movie_count, movies.size)
            movieAdapter.updateMovies(movies.toMutableList())
        }
    }

    private fun clearFilters() {
        // Clear all input fields
        binding.edtTitle.text?.clear()
        binding.edtDescription.text?.clear()
        binding.edtCastName.text?.clear()

        // Clear genre selections
        selectedGenres.clear()
        binding.chipGroupGenres.clearCheck()

        // Reset time slot spinner
        binding.spinnerTimeSlot.setSelection(0)

        // Reload all movies
        movieViewModel.getAllMovies()
        updateMovieResults(movieViewModel.movies.value ?: emptyList())

        Toast.makeText(this, "Đã xóa bộ lọc", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFilterSection() {
        if (binding.filterSection.isVisible) {
            binding.filterSection.visibility = View.GONE
        } else {
            binding.filterSection.visibility = View.VISIBLE
        }
    }
}


