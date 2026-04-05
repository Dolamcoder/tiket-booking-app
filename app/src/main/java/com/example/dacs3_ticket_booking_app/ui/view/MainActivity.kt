package com.example.dacs3_ticket_booking_app.ui.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Banner
import com.example.dacs3_ticket_booking_app.databinding.ActivityMainBinding
import com.example.dacs3_ticket_booking_app.ui.view.adaper.MovieAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BannerViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.ticketbookingapp.ui.view.adaper.BannerAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bannerViewModel: BannerViewModel
    private lateinit var movieViewModel: MovieViewModel
    private val sliderHandle= Handler(Looper.getMainLooper())
    private val sliderRunnable= Runnable{
        binding.viewPager2.currentItem=binding.viewPager2.currentItem+1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        // Initialize both ViewModels first
        bannerViewModel = ViewModelProvider(this).get(BannerViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // Observe LiveData after both ViewModels are initialized
        observeViewModel()

        // Load data
        bannerViewModel.getBanners()
        movieViewModel.getAllMovies()
    }

    private fun observeViewModel() {
        // Observe banners list
        bannerViewModel.banners.observe(this) { banners ->
            binding.progressBarSlider.visibility= View.VISIBLE
            banner(banners)
            binding.progressBarSlider.visibility= View.GONE
        }

        // Observe NOW SHOWING movies
        movieViewModel.nowShowingMovies.observe(this) { nowShowingMovies ->
            binding.recyclerViewTopMovies.layoutManager=
                LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewTopMovies.adapter= MovieAdapter(nowShowingMovies.toMutableList())
            binding.progressTopMovies.visibility= View.GONE
        }

        // Observe COMING SOON movies
        movieViewModel.comingSoonMovies.observe(this) { comingSoonMovies ->
            binding.recyclerViewUpcomingMovies.layoutManager=
                LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewUpcomingMovies.adapter= MovieAdapter(comingSoonMovies.toMutableList())
            binding.progressUpcomingMovies.visibility= View.GONE
        }

        // Observe loading state
        movieViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                println("Đang tải phim...")
            }
        }

        // Observe error messages
        movieViewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Observe success messages
        movieViewModel.successMessage.observe(this) { successMessage ->
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
        }
    }
    private fun banner(lists: List<Banner>) {
        binding.viewPager2.adapter= BannerAdapter(lists.toMutableList(), binding.viewPager2)
        binding.viewPager2.clipToPadding=false
        binding.viewPager2.clipChildren=false
        binding.viewPager2.offscreenPageLimit=3
        binding.viewPager2.getChildAt(0).overScrollMode= RecyclerView.OVER_SCROLL_NEVER
        val compositePageTransformer= CompositePageTransformer().apply {
            addTransformer (MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem=1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRunnable)
            }
        })
    }
}