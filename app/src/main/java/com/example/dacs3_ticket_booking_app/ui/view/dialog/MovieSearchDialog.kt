package com.example.dacs3_ticket_booking_app.ui.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.ui.view.adapter.MovieSearchAdapter

class MovieSearchDialog(
    context: Context,
    private val movies: List<Movie>,
    private val onMovieSelected: (Movie) -> Unit
) : Dialog(context) {

    private lateinit var etSearch: EditText
    private lateinit var ivClear: ImageView
    private lateinit var rvMovies: RecyclerView
    private lateinit var adapter: MovieSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_movie_search)

        // Full screen style
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupViews()
        setupRecyclerView()
        setupSearchListener()
    }

    private fun setupViews() {
        etSearch = findViewById(R.id.etSearchMovie)
        ivClear = findViewById(R.id.ivClear)
        rvMovies = findViewById(R.id.rvMovies)

        ivClear.setOnClickListener {
            etSearch.text.clear()
            adapter.filterByQuery("")
        }
    }

    private fun setupRecyclerView() {
        adapter = MovieSearchAdapter(
            movies = movies.toMutableList(),
            onMovieSelected = { movie ->
                onMovieSelected(movie)
                dismiss()
            }
        )
        rvMovies.adapter = adapter
        rvMovies.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                adapter.filterByQuery(query)
            }
        })
    }
}

