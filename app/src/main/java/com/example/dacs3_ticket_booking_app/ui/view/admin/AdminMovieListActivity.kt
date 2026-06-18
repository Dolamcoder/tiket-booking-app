package com.example.dacs3_ticket_booking_app.ui.view.admin
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminMovieListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminMovieAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
class AdminMovieListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMovieListBinding
    private lateinit var movieViewModel: MovieViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        setupRecyclerView()
        setupSearchListener()
        observeViewModel()
        binding.backBtn.setOnClickListener { finish() }
        binding.fabAddMovie.setOnClickListener {
            startActivity(Intent(this, AdminMovieFormActivity::class.java))
        }
        movieViewModel.getAllMovies()
    }
    override fun onResume() {
        super.onResume()
        movieViewModel.getAllMovies()
    }
    private fun setupRecyclerView() {
        binding.recyclerViewMovies.layoutManager = LinearLayoutManager(this)
    }
    private fun setupSearchListener() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    movieViewModel.getAllMovies()
                } else {
                    movieViewModel.searchMoviesByTitle(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun observeViewModel() {
        movieViewModel.movies.observe(this) { movies ->
            binding.recyclerViewMovies.adapter = AdminMovieAdapter(
                movies.toMutableList(),
                onEdit = { movie ->
                    val intent = Intent(this, AdminMovieFormActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                },
                onDetail = { movie ->
                    val intent = Intent(this, AdminMovieDetailActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                },
                onDelete = { movie ->
                    movieViewModel.deleteMovie(movie.id)
                }
            )
        }
        movieViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        movieViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        movieViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
