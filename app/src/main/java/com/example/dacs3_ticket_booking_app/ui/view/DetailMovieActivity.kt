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

import androidx.core.view.isVisible
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dacs3_ticket_booking_app.data.model.Review
import com.example.dacs3_ticket_booking_app.ui.view.adapter.ReviewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DetailMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMovieBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Window Insets for Keyboard
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            
            v.setPadding(systemBars.left, 0, systemBars.right, ime.bottom)
            
            if (isKeyboardVisible && binding.etReviewComment.isFocused) {
                binding.scrollContent.postDelayed({
                    binding.scrollContent.smoothScrollTo(0, binding.layoutAddReview.bottom)
                }, 100)
            }
            insets
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // Initialize Review Adapter
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(this@DetailMovieActivity)
        }

        // Get movie ID from intent
        val movieId = intent.getStringExtra("MOVIE_ID") ?: ""

        if (movieId.isNotEmpty()) {
            viewModel.getMovieById(movieId)
        }

        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Toggle review form
        binding.btnWriteReview.setOnClickListener {
            if (binding.layoutAddReview.isVisible) {
                binding.layoutAddReview.visibility = View.GONE
                binding.btnWriteReview.text = "Write a review"
                hideKeyboard()
            } else {
                binding.layoutAddReview.visibility = View.VISIBLE
                binding.btnWriteReview.text = "Cancel"
                binding.etReviewComment.requestFocus()
                // Scroll to the review form
                binding.scrollContent.postDelayed({
                    binding.scrollContent.smoothScrollTo(0, binding.layoutAddReview.top)
                }, 200)
            }
        }

        // Submit review
        binding.btnSubmitReview.setOnClickListener {
            hideKeyboard()
            val rating = binding.rbInputRating.rating
            val comment = binding.etReviewComment.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please login to write a review", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get user details from Firestore (simpler for this example, or use a cached user)
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("fullName") ?: currentUser.displayName ?: "Anonymous"
                    val userAvatar = document.getString("avatar") ?: ""

                    val review = Review(
                        movieId = movieId,
                        userId = currentUser.uid,
                        userName = userName,
                        userAvatar = userAvatar,
                        rating = rating,
                        comment = comment
                    )

                    viewModel.addReview(review)
                    
                    // Reset and hide form
                    binding.rbInputRating.rating = 0f
                    binding.etReviewComment.setText("")
                    binding.layoutAddReview.visibility = View.GONE
                    binding.btnWriteReview.text = "Write a review"
                    binding.scrollContent.smoothScrollTo(0, binding.reviewSection.top)
                }
        }

        // Handle IME action
        binding.etReviewComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                binding.etReviewComment.clearFocus()
                true
            } else {
                false
            }
        }

        binding.btnHideKeyboardUI.setOnClickListener {
            hideKeyboard()
            binding.etReviewComment.clearFocus()
        }

        // Observe reviews
        viewModel.reviews.observe(this) { reviews ->
            if (reviews.isNullOrEmpty()) {
                binding.tvNoReviews.visibility = View.VISIBLE
                binding.rvReviews.visibility = View.GONE
            } else {
                binding.tvNoReviews.visibility = View.GONE
                binding.rvReviews.visibility = View.VISIBLE
                reviewAdapter.updateReviews(reviews)
                
                // Update movie average rating UI if needed
                val avgRating = reviews.map { it.rating }.average()
                binding.tvImdbRating.text = "★${String.format(Locale.getDefault(), "%.1f", avgRating)}"
            }
        }

        // Observe reviews pagination
        viewModel.currentPage.observe(this) { page ->
            updatePaginationUI()
        }

        viewModel.totalReviews.observe(this) { total ->
            if (total > 0) {
                binding.layoutPagination.visibility = View.VISIBLE
                updatePaginationUI()
            } else {
                binding.layoutPagination.visibility = View.GONE
            }
        }

        binding.btnPrevPage.setOnClickListener {
            viewModel.loadPrevPage(movieId)
        }

        binding.btnNextPage.setOnClickListener {
            viewModel.loadNextPage(movieId)
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
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.successMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is android.widget.EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
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

    private fun updatePaginationUI() {
        val current = viewModel.currentPage.value ?: 1
        val totalItems = viewModel.totalReviews.value ?: 0
        val pageSize = 5
        val totalPages = Math.ceil(totalItems.toDouble() / pageSize).toInt()

        if (totalPages <= 1) {
            binding.layoutPagination.visibility = View.GONE
            return
        }

        binding.layoutPagination.visibility = View.VISIBLE
        binding.tvPageNumbers.text = "Trang $current / $totalPages"
        
        binding.btnPrevPage.isEnabled = current > 1
        binding.btnPrevPage.alpha = if (current > 1) 1.0f else 0.3f
        
        binding.btnNextPage.isEnabled = current < totalPages
        binding.btnNextPage.alpha = if (current < totalPages) 1.0f else 0.3f
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}