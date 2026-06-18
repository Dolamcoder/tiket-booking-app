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
import com.example.dacs3_ticket_booking_app.ui.view.adapter.RevenueAdapterItem
import com.example.dacs3_ticket_booking_app.ui.view.custom.DoughnutChartView
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

    private val paletteColors = listOf(
        android.graphics.Color.parseColor("#6366F1"),
        android.graphics.Color.parseColor("#EC4899"),
        android.graphics.Color.parseColor("#10B981"),
        android.graphics.Color.parseColor("#F59E0B"),
        android.graphics.Color.parseColor("#3B82F6"),
        android.graphics.Color.parseColor("#8B5CF6"),
        android.graphics.Color.parseColor("#EF4444"),
        android.graphics.Color.parseColor("#06B6D4")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRevenueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

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
            R.layout.spinner_item_white
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_white)
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
                val totalRevenue = revenueMap.values.sum()
                val revenuesList = revenueViewModel.revenues.value ?: emptyList()
                val movieTicketsMap = revenuesList.groupBy { it.movieTitle }
                    .mapValues { entry -> entry.value.sumOf { it.ticketCount } }

                val totalTickets = movieTicketsMap.values.sum()

                val sortedList = revenueMap.entries
                    .sortedByDescending { it.value }

                val adapterItems = sortedList.mapIndexed { index, entry ->
                    val revenue = entry.value
                    val pct = if (totalRevenue > 0) ((revenue / totalRevenue) * 100).toInt() else 0
                    val tickets = movieTicketsMap[entry.key] ?: 0
                    val color = paletteColors[index % paletteColors.size]
                    RevenueAdapterItem(
                        title = entry.key,
                        totalRevenue = revenue,
                        ticketCount = tickets,
                        percentage = pct,
                        color = color
                    )
                }

                revenueAdapter = RevenueAdapter(adapterItems)
                binding.recyclerViewMovieRevenue.adapter = revenueAdapter
                binding.tvRevenueTitle.text = "Doanh thu theo phim"

                // Update Doughnut Chart
                val slices = adapterItems.map {
                    DoughnutChartView.Slice(
                        label = it.title,
                        value = it.totalRevenue,
                        color = it.color
                    )
                }
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                binding.doughnutChartView.setData(
                    slices,
                    totalText = formatter.format(totalRevenue.toLong()) + "₫",
                    totalSubText = "$totalTickets vé"
                )
            }
        }
        
        revenueViewModel.revenueByRoom.observe(this) { roomMap ->
            if (currentViewType == 1) {
                val totalRevenue = roomMap.values.sumOf { it.first }
                val totalTickets = roomMap.values.sumOf { it.second }

                val sortedList = roomMap.entries
                    .sortedByDescending { it.value.first }

                val adapterItems = sortedList.mapIndexed { index, entry ->
                    val revenue = entry.value.first
                    val tickets = entry.value.second
                    val pct = if (totalRevenue > 0) ((revenue / totalRevenue) * 100).toInt() else 0
                    val color = paletteColors[index % paletteColors.size]
                    RevenueAdapterItem(
                        title = entry.key,
                        totalRevenue = revenue,
                        ticketCount = tickets,
                        percentage = pct,
                        color = color
                    )
                }

                revenueAdapter = RevenueAdapter(adapterItems)
                binding.recyclerViewMovieRevenue.adapter = revenueAdapter
                binding.tvRevenueTitle.text = "Doanh thu theo phòng"

                // Update Doughnut Chart
                val slices = adapterItems.map {
                    DoughnutChartView.Slice(
                        label = it.title,
                        value = it.totalRevenue,
                        color = it.color
                    )
                }
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                binding.doughnutChartView.setData(
                    slices,
                    totalText = formatter.format(totalRevenue.toLong()) + "₫",
                    totalSubText = "$totalTickets vé"
                )
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

