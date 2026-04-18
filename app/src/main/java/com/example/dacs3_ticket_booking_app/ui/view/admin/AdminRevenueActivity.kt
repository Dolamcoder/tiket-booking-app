package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminRevenueBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.RevenueAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RevenueViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminRevenueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRevenueBinding
    private lateinit var revenueViewModel: RevenueViewModel
    private lateinit var revenueAdapter: RevenueAdapter
    
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var startDate: Long = 0
    private var endDate: Long = 0
    private var currentViewType = 0  // 0 = by movie, 1 = by room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRevenueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        revenueViewModel = ViewModelProvider(this).get(RevenueViewModel::class.java)

        setupRecyclerView()
        setupSpinner()
        setupDatePickers()
        setupFilterButtons()
        observeViewModel()

        binding.backBtn.setOnClickListener { finish() }

        // Load doanh thu tất cả
        revenueViewModel.getAllRevenues()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMovieRevenue.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.revenue_view_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerViewType.adapter = adapter
        
        binding.spinnerViewType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentViewType = position
                refreshData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupDatePickers() {
        // Set default dates: today
        val cal = Calendar.getInstance()
        endDate = cal.timeInMillis
        cal.add(Calendar.MONTH, -1)
        startDate = cal.timeInMillis
        
        updateDateDisplay()
        
        binding.edtStartDate.setOnClickListener {
            showStartDatePicker()
        }
        
        binding.edtEndDate.setOnClickListener {
            showEndDatePicker()
        }
    }
    
    private fun showStartDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startDate
        
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, month, dayOfMonth, 0, 0, 0)
            startDate = selectedCal.timeInMillis
            updateDateDisplay()
            refreshData()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    
    private fun showEndDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = endDate
        
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, month, dayOfMonth, 23, 59, 59)
            endDate = selectedCal.timeInMillis
            updateDateDisplay()
            refreshData()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    
    private fun updateDateDisplay() {
        binding.edtStartDate.setText(sdf.format(startDate))
        binding.edtEndDate.setText(sdf.format(endDate))
    }

    private fun setupFilterButtons() {
        binding.btnToday.setOnClickListener {
            filterByToday()
        }
        binding.btnWeek.setOnClickListener {
            filterByWeek()
        }
        binding.btnMonth.setOnClickListener {
            filterByMonth()
        }
        binding.btnAll.setOnClickListener {
            filterByAll()
        }
    }

    private fun filterByToday() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        startDate = cal.timeInMillis
        
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        endDate = cal.timeInMillis
        
        updateDateDisplay()
        refreshData()
    }

    private fun filterByWeek() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -7)
        startDate = cal.timeInMillis
        endDate = System.currentTimeMillis()
        
        updateDateDisplay()
        refreshData()
    }

    private fun filterByMonth() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        startDate = cal.timeInMillis
        endDate = System.currentTimeMillis()
        
        updateDateDisplay()
        refreshData()
    }
    
    private fun filterByAll() {
        startDate = 0
        endDate = System.currentTimeMillis()
        
        updateDateDisplay()
        if (currentViewType == 0) {
            revenueViewModel.getAllRevenues()
        } else {
            revenueViewModel.getRevenueByRoom()
        }
    }
    
    private fun refreshData() {
        when (currentViewType) {
            0 -> revenueViewModel.getRevenueByDateRange(startDate, endDate)
            1 -> revenueViewModel.getRevenueByRoomDateRange(startDate, endDate)
        }
    }
    
    // ...existing code...
    private fun observeViewModel() {
        revenueViewModel.totalRevenue.observe(this) { total ->
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            binding.tvTotalRevenue.text = formatter.format(total.toLong()) + "₫"
        }

        revenueViewModel.totalTickets.observe(this) { tickets ->
            binding.tvTotalTickets.text = tickets.toString()
        }

        revenueViewModel.revenueByMovie.observe(this) { revenueMap ->
            if (currentViewType == 0) {
                val sortedList = revenueMap.entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                revenueAdapter = RevenueAdapter(sortedList)
                binding.recyclerViewMovieRevenue.adapter = revenueAdapter
                binding.tvRevenueTitle.text = "Doanh thu theo phim"
            }
        }
        
        revenueViewModel.revenueByRoom.observe(this) { roomMap ->
            if (currentViewType == 1) {
                val sortedList = roomMap.entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                revenueAdapter = RevenueAdapter(sortedList)
                binding.recyclerViewMovieRevenue.adapter = revenueAdapter
                binding.tvRevenueTitle.text = "Doanh thu theo phòng"
            }
        }

        revenueViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        revenueViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

