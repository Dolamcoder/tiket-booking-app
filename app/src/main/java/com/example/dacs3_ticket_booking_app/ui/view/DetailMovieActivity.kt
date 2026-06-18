package com.example.dacs3_ticket_booking_app.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.databinding.ActivityDetailMovieBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.CastAdapter
import com.example.dacs3_ticket_booking_app.ui.view.adapter.GenreAdapter
import com.example.dacs3_ticket_booking_app.ui.view.client.SeatListActivity
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel

class DetailMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMovieBinding
    private lateinit var viewModel: MovieViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityDetailMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
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

                // Setup trailer player
                setupTrailerPlayer(movie.trailer)

                // Set buy ticket button
                binding.btnBuyTicket.setOnClickListener {
                    val intent = Intent(this, SeatListActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
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

    private fun setupTrailerPlayer(trailerUrl: String) {
        if (trailerUrl.isEmpty()) {
            binding.trailerThumbnail.visibility = View.GONE
            binding.tvNoTrailer.visibility = View.VISIBLE
            return
        }

        val videoId = extractYoutubeVideoId(trailerUrl)
        if (videoId == null) {
            binding.trailerThumbnail.visibility = View.GONE
            binding.tvNoTrailer.visibility = View.VISIBLE
            return
        }

        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

        // Load YouTube thumbnail
        Glide.with(this)
            .load(thumbnailUrl)
            .placeholder(R.drawable.back_dark)
            .error(R.drawable.back_dark)
            .into(binding.ivTrailerThumbnail)

        var youTubePlayer: YouTubePlayer? = null
        var isPlayClicked = false

        // Đăng ký lifecycle cho youtubePlayerView
        lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                if (isPlayClicked) {
                    player.loadVideo(videoId, 0f)
                }
            }
        })

        // Bấm thumbnail -> hiển thị YouTubePlayerView và bắt đầu phát video
        binding.trailerThumbnail.setOnClickListener {
            binding.trailerThumbnail.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.trailerThumbnail.visibility = View.GONE
                    binding.trailerThumbnail.alpha = 1f
                    binding.youtubePlayerView.visibility = View.VISIBLE

                    isPlayClicked = true
                    youTubePlayer?.loadVideo(videoId, 0f)
                }.start()
        }
    }

    private fun extractYoutubeVideoId(url: String): String? {
        return when {
            url.contains("youtu.be/") ->
                url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&").trim()
            url.contains("youtube.com/watch") -> {
                try { Uri.parse(url).getQueryParameter("v") } catch (e: Exception) { null }
            }
            url.contains("youtube.com/embed/") ->
                url.substringAfter("embed/").substringBefore("?").trim()
            url.contains("youtube.com/shorts/") ->
                url.substringAfter("shorts/").substringBefore("?").trim()
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}