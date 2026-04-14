package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminRevenueBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.RevenueAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RevenueViewModel
import java.text.NumberFormat
import java.util.Locale

class AdminRevenueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRevenueBinding
    private lateinit var revenueViewModel: RevenueViewModel
    private lateinit var revenueAdapter: RevenueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRevenueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        revenueViewModel = ViewModelProvider(this).get(RevenueViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        binding.backBtn.setOnClickListener { finish() }

        revenueViewModel.getAllRevenues()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMovieRevenue.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        revenueViewModel.totalRevenue.observe(this) { total ->
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            binding.tvTotalRevenue.text = formatter.format(total.toLong()) + "₫"
        }

        revenueViewModel.totalTickets.observe(this) { tickets ->
            binding.tvTotalTickets.text = tickets.toString()
        }

        revenueViewModel.revenueByMovie.observe(this) { revenueMap ->
            val sortedList = revenueMap.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            revenueAdapter = RevenueAdapter(sortedList)
            binding.recyclerViewMovieRevenue.adapter = revenueAdapter
        }

        revenueViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        revenueViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

