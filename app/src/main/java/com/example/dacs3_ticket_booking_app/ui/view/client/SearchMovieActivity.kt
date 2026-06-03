package com.example.dacs3_ticket_booking_app.ui.view.client

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ActivitySearchMovieBinding
import com.example.dacs3_ticket_booking_app.ui.view.MainActivity
import com.example.dacs3_ticket_booking_app.ui.view.adaper.MovieAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.GenreViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.utils.SpeechToTextUtil
import com.google.android.material.chip.Chip

class SearchMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchMovieBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var genreViewModel: GenreViewModel
    private lateinit var movieAdapter: MovieAdapter

    // Voice Search
    private lateinit var speechToTextUtil: SpeechToTextUtil
    private var currentSpeechTarget: EditText? = null

    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        speechToTextUtil.handleSpeechResult(result.resultCode, result.data)
    }

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

        // Setup Navigation
        binding.chipNavigation.setItemSelected(R.id.search, true)
        binding.chipNavigation.setOnItemSelectedListener { id ->
            when (id) {
                R.id.profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.home -> startActivity(Intent(this, MainActivity::class.java))
            }
        }

        // Initialize ViewModels
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        genreViewModel = ViewModelProvider(this).get(GenreViewModel::class.java)

        // Init Voice
        initSpeechToText()

        // Setup UI
        setupUI()
        setupRecyclerView()
        observeViewModels()

        // Load data
        movieViewModel.getAllMovies()
        genreViewModel.getAllGenres()
    }

    // ==================== VOICE SEARCH ====================
    private fun initSpeechToText() {
        speechToTextUtil = SpeechToTextUtil(
            context = this,
            onResult = { recognizedText ->
                currentSpeechTarget?.let { editText ->
                    editText.setText(recognizedText)
                    Toast.makeText(this, "Đã nhập: $recognizedText", Toast.LENGTH_SHORT).show()
                }
                currentSpeechTarget = null
            },
            onError = { errorMsg ->
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                currentSpeechTarget = null
            }
        )

        setupVoiceInput()
    }

    private fun setupVoiceInput() {
        // Setup microphone for Title
        setupMicrophoneClick(binding.edtTitle)

        // Setup microphone for Description
        setupMicrophoneClick(binding.edtDescription)

        // Setup microphone for Cast Name
        setupMicrophoneClick(binding.edtCastName)
    }

    private fun setupMicrophoneClick(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null &&
                    event.x >= (editText.width - editText.paddingRight - drawableEnd.intrinsicWidth)
                ) {
                    currentSpeechTarget = editText

                    if (speechToTextUtil.hasMicrophonePermission()) {
                        speechToTextUtil.startListening(speechRecognitionLauncher)
                    } else {
                        requestMicrophonePermission()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun requestMicrophonePermission() {
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_SPEECH
        )
    }

    // ==================== OTHER SETUP ====================
    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSearch.setOnClickListener { performAdvancedSearch() }

        binding.btnClear.setOnClickListener { clearFilters() }

        setupTimeSlotSpinner()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(mutableListOf(), null)
        binding.recyclerViewResults.apply {
            layoutManager = GridLayoutManager(this@SearchMovieActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun setupTimeSlotSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item_white, timeSlotsData)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_white)
        binding.spinnerTimeSlot.adapter = adapter

        binding.spinnerTimeSlot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModels() {
        movieViewModel.searchResults.observe(this) { results ->
            updateMovieResults(results)
        }

        movieViewModel.movies.observe(this) { movies ->
            if (movies.isNotEmpty() && binding.edtTitle.text.isNullOrEmpty()) {
                updateMovieResults(movies)
            }
        }

        genreViewModel.genres.observe(this) { genres ->
            setupGenreChips(genres)
        }

        movieViewModel.isLoading.observe(this) { isLoading ->
            binding.progressSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

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
                    if (isChecked) selectedGenres.add(genre.name)
                    else selectedGenres.remove(genre.name)
                }
            }
            binding.chipGroupGenres.addView(chip)
        }
    }

    private fun performAdvancedSearch() {
        val title = binding.edtTitle.text.toString().ifEmpty { null }
        val description = binding.edtDescription.text.toString().ifEmpty { null }
        val castName = binding.edtCastName.text.toString().ifEmpty { null }

        val selectedTimeSlot = binding.spinnerTimeSlot.selectedItem as String
        val priceTier = timeSlotsMap[selectedTimeSlot]
        val genresList = selectedGenres.ifEmpty { null }?.toList()

        binding.progressSearch.visibility = View.VISIBLE

        movieViewModel.advancedSearch(
            title = title,
            description = description,
            genres = genresList,
            castName = castName,
            priceTier = priceTier
        )
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
        binding.edtTitle.text?.clear()
        binding.edtDescription.text?.clear()
        binding.edtCastName.text?.clear()

        selectedGenres.clear()
        binding.chipGroupGenres.clearCheck()
        binding.spinnerTimeSlot.setSelection(0)

        movieViewModel.getAllMovies()
        updateMovieResults(movieViewModel.movies.value ?: emptyList())

        Toast.makeText(this, "Đã xóa bộ lọc", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_CODE_SPEECH = 100
    }
}