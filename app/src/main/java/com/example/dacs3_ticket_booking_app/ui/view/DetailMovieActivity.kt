package com.example.dacs3_ticket_booking_app.ui.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.databinding.ActivityDetailMovieBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.CastAdapter
import com.example.dacs3_ticket_booking_app.ui.view.adapter.GenreAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import kotlin.math.roundToInt

class DetailMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMovieBinding
    private lateinit var viewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // Get movie ID from intent
        val movieId = intent.getStringExtra("MOVIE_ID") ?: ""

        if (movieId.isNotEmpty()) {
            viewModel.getMovieById(movieId)
        }

        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Observe movie detail
        viewModel.movieDetail.observe(this) { movie ->
            if (movie != null) {
                // Set title
                binding.tvMovieTitle.text = movie.title

                // Set poster image
                if (movie.poster.isNotEmpty()) {
                    Glide.with(this)
                        .load(movie.poster)
                        .placeholder(R.drawable.back_dark)
                        .error(R.drawable.back_dark)
                        .into(binding.ivPoster)
                }

                // Set genres
                if (movie.genres.isNotEmpty()) {
                    val genreAdapter = GenreAdapter(movie.genres)
                    binding.rvGenres.adapter = genreAdapter
                    binding.rvGenres.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                }

                // Set year and duration
                val durationHours = movie.duration/60
                val durationMinutes = (movie.duration - durationHours*60)
                binding.tvYearDuration.text = "${movie.year} - ${durationHours}hour ${durationMinutes}minutes"

                // Set description
                binding.tvDescription.text = movie.description

                // Set casts
                if (movie.casts.isNotEmpty()) {
                    val castAdapter = CastAdapter(movie.casts)
                    binding.rvCasts.adapter = castAdapter
                    binding.rvCasts.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                }

                // Set buy ticket button
                binding.btnBuyTicket.setOnClickListener {
                    // TODO: Navigate to booking screen
                }
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: Show/hide loading indicator
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            // TODO: Show error toast or snackbar
        }
    }
}