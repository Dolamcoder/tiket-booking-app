package com.example.dacs3_ticket_booking_app.ui.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.dacs3_ticket_booking_app.ui.view.client.ProfileActivity
import com.example.dacs3_ticket_booking_app.ui.view.client.SearchMovieActivity
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BannerViewModel
import com.example.dacs3_ticket_booking_app.ui.viewmodel.MovieViewModel
import com.example.dacs3_ticket_booking_app.utils.SpeechToTextUtil
import com.example.ticketbookingapp.ui.view.adaper.BannerAdapter
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bannerViewModel: BannerViewModel
    private lateinit var movieViewModel: MovieViewModel
    private val sliderHandle= Handler(Looper.getMainLooper())
    private val sliderRunnable= Runnable{
        binding.viewPager2.currentItem=binding.viewPager2.currentItem+1
    }
    
    private lateinit var speechToTextUtil: SpeechToTextUtil
    
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        speechToTextUtil.handleSpeechResult(result.resultCode, result.data)
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
        
        initSpeechToText()
        binding.chipNavigation.setItemSelected(R.id.home, true)
        binding.chipNavigation.setOnItemSelectedListener(object : com.ismaeldivita.chipnavigation.ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                when (id) {
                    R.id.profile -> {
                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    }
                    R.id.search -> {
                        startActivity(Intent(this@MainActivity, SearchMovieActivity::class.java))
                    }
                    R.id.chat_ai -> {
                        startActivity(Intent(this@MainActivity, ChatActivity::class.java))
                    }
                    R.id.home -> {
                        // TODO: Xử lý Explorer
                    }
                }
            }
        })
        
        // ✅ Hiển thị tên người dùng
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userName = currentUser.displayName ?: "User"
            binding.textView.text = "Hello ${userName.split(" ").lastOrNull() ?: userName}"
            println("User: $userName")
        }
        
        bannerViewModel = ViewModelProvider(this).get(BannerViewModel::class.java)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // Observe LiveData after both ViewModels are initialized
        observeViewModel()

        // Load data
        bannerViewModel.getBanners()
        movieViewModel.getAllMovies()
    }
    
    // ✅ Khởi tạo Speech to Text
    private fun initSpeechToText() {
        speechToTextUtil = SpeechToTextUtil(
            context = this,
            onResult = { recognizedText ->
                // ✅ Cập nhật text vào search box
                binding.searchEditText.setText(recognizedText)
                Toast.makeText(this, "Tìm kiếm: $recognizedText", Toast.LENGTH_SHORT).show()
                // ✅ Optional: Thực hiện tìm kiếm ngay
                searchMovies(recognizedText)
            },
            onError = { errorMsg ->
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
        
        // ✅ Setup click listener cho icon microphone
        setupSearchUI()
    }
    
     // ✅ Setup Search UI
    private fun setupSearchUI() {
        binding.searchEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Optional: Xóa text placeholder khi focus
                if (binding.searchEditText.text.toString() == "Tên film") {
                    binding.searchEditText.text.clear()
                }
            }
        }
        
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 🔥 Real-time search - auto search khi text thay đổi
                val searchQuery = s.toString().trim()
                if (searchQuery.isNotEmpty() && searchQuery != "Tên film") {
                    android.util.Log.d("MainActivity", "🔍 Real-time search: $searchQuery")
                    movieViewModel.searchMoviesByTitle(searchQuery)
                } else if (searchQuery.isEmpty()) {
                    // Quay lại danh sách bình thường
                    movieViewModel.getAllMovies()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Bắt sự kiện click vào icon microphone (drawable end)
        binding.searchEditText.setOnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = binding.searchEditText.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null && event.x >= (binding.searchEditText.width - binding.searchEditText.paddingRight - drawableEnd.intrinsicWidth)) {
                    // ✅ Nhấn vào microphone icon
                    if (speechToTextUtil.hasMicrophonePermission()) {
                        speechToTextUtil.startListening(speechRecognitionLauncher)
                    } else {
                        requestMicrophonePermission()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
        
        // ✅ Bắt sự kiện Enter để tìm kiếm (backup)
        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = binding.searchEditText.text.toString().trim()
                if (searchQuery.isNotEmpty() && searchQuery != "Tên film") {
                    movieViewModel.searchMoviesByTitle(searchQuery)
                }
                true
            } else {
                false
            }
        }
    }
    
    // ✅ Tìm kiếm phim (giữ lại cho compatibility)
    private fun searchMovies(query: String) {
        if (query.isEmpty() || query == "Tên film") {
            movieViewModel.getAllMovies()
            return
        }

        android.util.Log.d("MainActivity", "🔍 Tìm kiếm phim: $query")

        movieViewModel.searchMoviesByTitle(query)
    }
    // ✅ Request quyền Microphone
    private fun requestMicrophonePermission() {
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_SPEECH
        )
    }

    companion object {
        const val REQUEST_CODE_SPEECH = 100
    }

    private fun observeViewModel() {
        // Observe banners list
        bannerViewModel.banners.observe(this) { banners ->
            binding.progressBarSlider.visibility= View.VISIBLE
            banner(banners)
            binding.progressBarSlider.visibility= View.GONE
        }

        // 🔥 Observe NOW SHOWING movies - tự động update khi search
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

        // 🔥 Observe COMING SOON movies - tự động update khi search
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