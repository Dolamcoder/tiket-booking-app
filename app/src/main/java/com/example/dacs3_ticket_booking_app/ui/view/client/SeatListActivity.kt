package com.example.dacs3_ticket_booking_app.ui.view.client

import android.os.Bundle
import android.view.View.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.data.model.Movie
import com.example.dacs3_ticket_booking_app.databinding.ActivitySeatListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.DateAdapter
import com.example.dacs3_ticket_booking_app.ui.view.adapter.TimeAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var movie: Movie
    private var price:Double=0.0
    private var number:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or SYSTEM_UI_FLAG_FULLSCREEN
                        or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        getIntentExtra()
        backOnclick()
        initTimeDateList()
    }
    private fun backOnclick(){
        binding.backBtn.setOnClickListener(){
            finish()
        }
    }
    private fun getIntentExtra(){
        movie=intent.getSerializableExtra("movie") as Movie

    }
    private fun initTimeDateList(){
        binding.apply{
            dateRecyclerView.layoutManager=
                LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
                dateRecyclerView.adapter= DateAdapter(generateDates())
            timeRecyclerView.layoutManager=
                LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            timeRecyclerView.adapter= TimeAdapter(generateTimes())

        }
    }
    private fun generateDates(): List<String> {
        val dates = mutableListOf<String>()
        val today= LocalDate.now()
        val formatter= DateTimeFormatter.ofPattern("EEE/dd/MMM")
        for(i in 0 until 7){
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        return dates
    }
    private fun generateTimes(): List<String> {
        val timeSlots=mutableListOf<String>()
        val formatter= DateTimeFormatter.ofPattern("hh:mm a")
        for(i in 0 until 24 step 2){
            val time= LocalDate.now().atTime(i,0)
            timeSlots.add(time.format(formatter))
        }
        return timeSlots
    }
}