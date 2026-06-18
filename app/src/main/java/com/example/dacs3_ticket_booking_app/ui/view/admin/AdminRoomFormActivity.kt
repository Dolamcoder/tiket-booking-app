package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_ticket_booking_app.data.model.Room
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminRoomFormBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel

class AdminRoomFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRoomFormBinding
    private lateinit var roomViewModel: RoomViewModel
    private var editingRoom: Room? = null
    private var currentLayout: MutableList<String> = mutableListOf()  // Lưu layout đang edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRoomFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)
        editingRoom = intent.getSerializableExtra("room") as? Room

        populateForm()
        setupButtons()
        observeViewModel()
    }

    private fun populateForm() {
        editingRoom?.let { room ->
            binding.titleBar.text = "Sửa phòng chiếu"
            binding.etRoomName.setText(room.name)
            binding.etRowCount.setText(room.rowCount.toString())
            binding.etColCount.setText("8")
            binding.etColCount.isEnabled = false
            binding.etSeatLayout.setText(room.seatLayout.joinToString("\n"))
            currentLayout = room.seatLayout.toMutableList()
        } ?: run {
            binding.titleBar.text = "Thêm phòng chiếu"
            binding.etColCount.setText("8")
            binding.etColCount.isEnabled = false
            currentLayout = mutableListOf()
        }
    }

    private fun setupButtons() {
        binding.backBtn.setOnClickListener { finish() }
        binding.btnGenerateLayout.setOnClickListener { generateLayout() }
        binding.btnSave.setOnClickListener { saveRoom() }
    }

    /**
     * Tự động sinh layout 8 cột x N hàng
     * LUÔN tạo 8 ký tự '1' (ghế ngồi) mỗi hàng
     */
    private fun generateLayout() {
        val rows = binding.etRowCount.text.toString().toIntOrNull()

        if (rows == null || rows <= 0) {
            Toast.makeText(this, "Vui lòng nhập số hàng hợp lệ (tối thiểu 1)", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo 8 cột, tất cả là '1' (ghế)
        currentLayout = (0 until rows).map { "11111111" }.toMutableList()

        binding.etSeatLayout.setText(currentLayout.joinToString("\n"))
        binding.etColCount.setText("8")
        Toast.makeText(this, "Đã tạo layout $rows hàng x 8 cột (${rows * 8} ghế)", Toast.LENGTH_SHORT).show()
    }

    private fun saveRoom() {
        val name = binding.etRoomName.text.toString().trim()
        val rowCount = binding.etRowCount.text.toString().toIntOrNull() ?: 0
        val colCount = 8
        
        if (name.isEmpty() || rowCount == 0) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin phòng", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse layout từ EditText hoặc dùng currentLayout
        val layoutRaw = binding.etSeatLayout.text.toString().trim()
        if (layoutRaw.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo layout phòng", Toast.LENGTH_SHORT).show()
            return
        }
        
        val seatLayout = layoutRaw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        // Validate: mỗi hàng phải có đúng 8 ký tự ('1'=ghế, '0'=trống)
        val isValid = seatLayout.all { row ->
            row.length == 8 && row.all { it == '1' || it == '0' }
        }
        if (!isValid) {
            Toast.makeText(this, "Layout không hợp lệ! Mỗi hàng phải có đúng 8 ký tự ('1'=ghế, '0'=trống)", Toast.LENGTH_SHORT).show()
            return
        }

        val room = editingRoom?.copy(
            name = name,
            rowCount = rowCount,
            colCount = colCount,
            seatLayout = seatLayout
        ) ?: Room(
            name = name,
            rowCount = rowCount,
            colCount = colCount,
            seatLayout = seatLayout
        )

        if (editingRoom != null) {
            roomViewModel.updateRoom(room)
        } else {
            roomViewModel.addRoom(room)
        }
    }

    private fun observeViewModel() {
        roomViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
        roomViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

