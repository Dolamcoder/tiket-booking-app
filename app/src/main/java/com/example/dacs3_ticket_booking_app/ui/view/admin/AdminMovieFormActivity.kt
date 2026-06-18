package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.app.Dialog
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Cast
import com.example.dacs3_ticket_booking_app.data.model.Genre
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminMovieFormBinding
import com.example.dacs3_ticket_booking_app.databinding.DialogAddCastBinding
import com.example.dacs3_ticket_booking_app.databinding.DialogAddGenreBinding
import com.example.dacs3_ticket_booking_app.databinding.DialogSelectCastBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.GenreSelectionAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.GenreViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.utils.CloudinaryHelper
import com.google.android.material.chip.Chip

class AdminMovieFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMovieFormBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var genreViewModel: GenreViewModel
    private var editingMovie: Movie? = null
    private var selectedImageUri: Uri? = null
    private var selectedGenres = mutableListOf<Genre>()
    private var isFormPopulated = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.posterPreview.setImageURI(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMovieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        genreViewModel = ViewModelProvider(this).get(GenreViewModel::class.java)
        editingMovie = intent.getSerializableExtra("movie") as? Movie

        setupButtons()
        // Điền dữ liệu nếu là sửa phim, nếu không thì hiện tiêu đề thêm phim
        if (editingMovie != null) {
            populateForm()
        } else {
            binding.titleBar.text = "Thêm phim"
        }
        observeViewModels()
        loadGenresAndCasts()
    }

    private fun loadGenresAndCasts() {
        genreViewModel.getAllGenres()
    }

    private fun populateForm() {
        editingMovie?.let { movie ->
            binding.titleBar.text = "Sửa phim"
            binding.etTitle.setText(movie.title)
            binding.etDescription.setText(movie.description)
            binding.etDuration.setText(movie.duration.toString())
            binding.etYear.setText(movie.year.toString())
            binding.etReleaseDate.setText(movie.releaseDate)
            binding.etTrailerUrl.setText(movie.trailer)
            
            // Hiển thị ảnh poster hiện tại của phim nếu có
            if (movie.poster.isNotEmpty()) {
                Glide.with(this)
                    .load(movie.poster)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.posterPreview)
            }
            
            // Display genres from movie directly
            displayGenresFromMovie()
        } ?: run {
            binding.titleBar.text = "Thêm phim"
        }
    }

    private fun setupButtons() {
        binding.backBtn.setOnClickListener { finish() }

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSelectGenre.setOnClickListener {
            showSelectGenreDialog()
        }

        binding.btnAddGenre.setOnClickListener {
            showAddGenreDialog()
        }

        binding.btnSave.setOnClickListener {
            saveMovie()
        }
    }

    private fun displaySelectedGenres() {
        try {
            binding.chipGroupGenres.removeAllViews()
            selectedGenres.forEach { genre ->
                val chip = Chip(this)
                chip.text = genre.name
                chip.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary))
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.admin_accent_soft)
                )
                chip.closeIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.admin_text_primary)
                )
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    selectedGenres.remove(genre)
                    displaySelectedGenres()
                }
                binding.chipGroupGenres.addView(chip)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayGenresFromMovie() {
        try {
            binding.chipGroupGenres.removeAllViews()
            editingMovie?.genres?.forEach { genreName ->
                val chip = Chip(this)
                chip.text = genreName
                chip.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary))
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.admin_accent_soft)
                )
                chip.closeIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.admin_text_primary)
                )
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    editingMovie = editingMovie?.copy(
                        genres = editingMovie!!.genres.filter { it != genreName }
                    )
                    selectedGenres = selectedGenres.filter { it.name != genreName }.toMutableList()
                    displayGenresFromMovie()
                }
                binding.chipGroupGenres.addView(chip)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSelectGenreDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val genreDialogView = layoutInflater.inflate(R.layout.dialog_select_genre, null)
        dialog.setContentView(genreDialogView)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        val allGenres = genreViewModel.genres.value ?: emptyList()
        val recyclerView = genreDialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.recyclerViewSelectGenre
        )
        val searchInput = genreDialogView.findViewById<android.widget.EditText>(
            R.id.etSearchGenre
        )
        val btnConfirm = genreDialogView.findViewById<android.widget.Button>(
            R.id.btnConfirmSelectGenre
        )
        val btnCancel = genreDialogView.findViewById<android.widget.Button>(
            R.id.btnCancelSelectGenre
        )
        val btnClose = genreDialogView.findViewById<android.widget.ImageButton>(
            R.id.btnCloseGenreDialog
        )

        val adapter = GenreSelectionAdapter(allGenres) { _, _ -> }
        adapter.setSelectedGenres(selectedGenres)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allGenres
                } else {
                    allGenres.filter { it.name.contains(query, ignoreCase = true) }
                }
                val filterAdapter = GenreSelectionAdapter(filtered) { _, _ -> }
                filterAdapter.setSelectedGenres(selectedGenres)
                recyclerView.adapter = filterAdapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnConfirm.setOnClickListener {
            selectedGenres = adapter.getSelectedGenres().toMutableList()
            displaySelectedGenres()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddGenreDialog() {
        val dialogBinding = DialogAddGenreBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnConfirmAddGenre.setOnClickListener {
            val genreName = dialogBinding.etGenreName.text.toString().trim()
            if (genreName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên thể loại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newGenre = Genre(name = genreName)
            genreViewModel.addGenre(newGenre)
            dialog.dismiss()
        }

        dialogBinding.btnCancelAddGenre.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveMovie() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val durationStr = binding.etDuration.text.toString().trim()
        val yearStr = binding.etYear.text.toString().trim()
        val releaseDate = binding.etReleaseDate.text.toString().trim()
        val trailerUrl = binding.etTrailerUrl.text.toString().trim()

        if (title.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên phim và thời lượng", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = durationStr.toIntOrNull() ?: 0
        val year = yearStr.toIntOrNull() ?: 0
        val genres = selectedGenres.map { it.name }
        val status = "coming_soon" // Default status

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if (selectedImageUri != null) {
            CloudinaryHelper.uploadImage(selectedImageUri!!, this) { posterUrl ->
                runOnUiThread {
                    if (posterUrl != null) {
                        persistMovie(title, description, duration, year, releaseDate, trailerUrl, genres, status, posterUrl)
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val existingPoster = editingMovie?.poster ?: ""
            persistMovie(title, description, duration, year, releaseDate, trailerUrl, genres, status, existingPoster)
        }
    }

    private fun persistMovie(
        title: String, description: String, duration: Int, year: Int,
        releaseDate: String, trailerUrl: String, genres: List<String>,
        status: String, posterUrl: String
    ) {
        val movie = editingMovie?.copy(
            title = title,
            description = description,
            duration = duration,
            year = year,
            releaseDate = releaseDate,
            trailer = trailerUrl,
            genres = genres,
            status = status,
            poster = posterUrl
        ) ?: Movie(
            title = title,
            description = description,
            duration = duration,
            year = year,
            releaseDate = releaseDate,
            trailer = trailerUrl,
            genres = genres,
            status = status,
            poster = posterUrl
        )

        if (editingMovie != null) {
            movieViewModel.updateMovie(movie)
        } else {
            movieViewModel.addMovie(movie)
        }
    }

    private fun observeViewModels() {
        movieViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
        movieViewModel.errorMessage.observe(this) { msg ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        genreViewModel.genres.observe(this) { genres ->
            if (editingMovie != null) {
                val allGenres = genres
                selectedGenres = allGenres.filter { genre ->
                    editingMovie!!.genres.contains(genre.name)
                }.toMutableList()
                // Display genres trực tiếp từ movie
                displayGenresFromMovie()
                isFormPopulated = true
            }
        }

        genreViewModel.successMessage.observe(this) { _ ->
            loadGenresAndCasts()
        }
    }
}
