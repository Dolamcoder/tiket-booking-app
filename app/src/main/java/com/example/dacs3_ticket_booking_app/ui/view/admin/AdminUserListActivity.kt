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
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminUserListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminUserAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminUserListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserListBinding
    private lateinit var userViewModel: UserViewModel
    private var allUsers = listOf<com.example.dacs3_ticket_booking_app.data.model.User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        setupRecyclerView()
        setupSearchListener()
        observeViewModel()

        binding.backBtn.setOnClickListener { finish() }
        binding.fabAddUser.setOnClickListener {
            startActivity(Intent(this, AdminUserFormActivity::class.java))
        }

        userViewModel.getAllUsers()
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getAllUsers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchListener() {
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                
                // Filter users real-time
                val filtered = if (query.isEmpty()) {
                    allUsers
                } else {
                    allUsers.filter { user ->
                        user.fullName.lowercase().contains(query) ||
                        user.email.lowercase().contains(query)
                    }
                }
                
                updateAdapter(filtered)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        userViewModel.users.observe(this) { users ->
            // Lọc bỏ user hiện tại đang đăng nhập
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            allUsers = users.filter { it.id != currentUserId }
            
            // Cập nhật adapter với dữ liệu hiện tại (hoặc kết quả tìm kiếm)
            val query = binding.etSearchUser.text.toString().trim().lowercase()
            val filtered = if (query.isEmpty()) {
                allUsers
            } else {
                allUsers.filter { user ->
                    user.fullName.lowercase().contains(query) ||
                    user.email.lowercase().contains(query)
                }
            }
            
            updateAdapter(filtered)
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        userViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        userViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAdapter(users: List<com.example.dacs3_ticket_booking_app.data.model.User>) {
        binding.recyclerViewUsers.adapter = AdminUserAdapter(
            users.toMutableList(),
            onEdit = { user ->
                val intent = Intent(this, AdminUserFormActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            },
            onDelete = { user ->
                userViewModel.deleteUser(user.id)
            }
        )
    }
}

