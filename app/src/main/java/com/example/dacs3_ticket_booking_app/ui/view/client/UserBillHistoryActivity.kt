package com.example.dacs3_ticket_booking_app.ui.view.client

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityUserBillHistoryBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.UserBillAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel
import com.google.firebase.auth.FirebaseAuth

class UserBillHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBillHistoryBinding
    private lateinit var billViewModel: BillViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBillHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

        // ✅ Nút quay lại
        binding.backBtn.setOnClickListener { finish() }

        // ✅ Setup RecyclerView
        binding.recyclerViewBills.layoutManager = LinearLayoutManager(this)

        // ✅ Lấy userId từ Firebase
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            observeViewModel()
            billViewModel.getBillsByUser(userId)
        } else {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeViewModel() {
        billViewModel.bills.observe(this) { bills ->
            binding.recyclerViewBills.adapter = UserBillAdapter(bills.toMutableList())
        }

        billViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        billViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

