package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminMovieDetailBinding

class AdminMovieDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMovieDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        val movie = intent.getSerializableExtra("movie") as? Movie

        if (movie != null) {
            displayMovieDetail(movie)
        } else {
            Toast.makeText(this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.backBtn.setOnClickListener { finish() }

    }

    private fun displayMovieDetail(movie: Movie) {
        // Poster
        if (movie.poster.isNotEmpty()) {
            Glide.with(this)
                .load(movie.poster)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivPoster)
        }

        binding.tvTitle.text = movie.title
        binding.tvDescription.text = movie.description.ifEmpty { "Chưa có mô tả" }
        binding.tvDuration.text = "${movie.duration} phút"
        binding.tvYear.text = movie.year.toString()
        binding.tvReleaseDate.text = movie.releaseDate.ifEmpty { "--/--/----" }
        binding.tvMovieId.text = "ID: ${movie.id}"
        binding.tvTrailer.text = movie.trailer.ifEmpty { "(chưa có)" }

        // Setup trailer player
        setupTrailerPlayer(movie.trailer)

        // Status badge
        val (statusText, statusColor) = when (movie.status) {
            "now_showing" -> Pair("Đang chiếu", "#4CAF50")
            "coming_soon" -> Pair("Sắp chiếu", "#FF9800")
            "ended"       -> Pair("Đã kết thúc", "#9E9E9E")
            else          -> Pair(movie.status, "#FF7043")
        }
        binding.tvStatus.text = statusText
        binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor(statusColor))

        // Genres
        binding.tvGenres.text = if (movie.genres.isNotEmpty())
            movie.genres.joinToString(" • ") else "Chưa có thể loại"

        // Cast
        val castCount = movie.casts.size
        binding.tvCastsCount.text = "$castCount người"

        binding.castContainer.removeAllViews()
        movie.casts.forEach { cast ->
            // Create a horizontal layout for avatar + name
            val castItemView = LinearLayout(this)
            castItemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            castItemView.orientation = LinearLayout.HORIZONTAL
            castItemView.gravity = android.view.Gravity.CENTER_VERTICAL
            castItemView.background = android.graphics.drawable.ColorDrawable(
                android.graphics.Color.parseColor("#1A1F3A")
            )
            castItemView.setPadding(12, 12, 12, 12)

            // Avatar ImageView
            val ivAvatar = ImageView(this)
            ivAvatar.layoutParams = LinearLayout.LayoutParams(
                80,
                80
            ).apply {
                marginEnd = 12
            }
            ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            ivAvatar.contentDescription = cast.name
            
            if (cast.images.isNotEmpty()) {
                Glide.with(this)
                    .load(cast.images)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivAvatar)
            }

            // Name TextView
            val tvName = TextView(this)
            tvName.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            tvName.text = cast.name
            tvName.setTextColor(android.graphics.Color.WHITE)
            tvName.textSize = 14f
            tvName.setTypeface(null, android.graphics.Typeface.BOLD)

            castItemView.addView(ivAvatar)
            castItemView.addView(tvName)
            binding.castContainer.addView(castItemView)
        }

        if (castCount == 0) {
            val emptyView = TextView(this)
            emptyView.text = "Chưa có thông tin diễn viên"
            emptyView.setTextColor(android.graphics.Color.parseColor("#888888"))
            emptyView.textSize = 12f
            binding.castContainer.addView(emptyView)
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
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
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
