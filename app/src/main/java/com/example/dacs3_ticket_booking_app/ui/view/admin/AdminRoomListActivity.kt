package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminRoomListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminRoomAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.RoomViewModel

class AdminRoomListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRoomListBinding
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomViewModel = ViewModelProvider(this).get(RoomViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.fabAddRoom.setOnClickListener {
            startActivity(Intent(this, AdminRoomFormActivity::class.java))
        }

        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(this)
        observeViewModel()
        roomViewModel.getAllRooms()
    }

    override fun onResume() {
        super.onResume()
        roomViewModel.getAllRooms()
    }

    private fun observeViewModel() {
        roomViewModel.rooms.observe(this) { rooms ->
            binding.recyclerViewRooms.adapter = AdminRoomAdapter(
                rooms.toMutableList(),
                onEdit = { room ->
                    val intent = Intent(this, AdminRoomFormActivity::class.java)
                    intent.putExtra("room", room)
                    startActivity(intent)
                },
                onDelete = { room -> roomViewModel.deleteRoom(room.id) },
                onDetail = { room ->
                    val intent = Intent(this, AdminRoomDetailActivity::class.java)
                    intent.putExtra("room", room)
                    startActivity(intent)
                }
            )
        }

        roomViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        roomViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        roomViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
