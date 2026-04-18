package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminBillListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminBillAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.BillViewModel

class AdminBillListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBillListBinding
    private lateinit var billViewModel: BillViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBillListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.recyclerViewBills.layoutManager = LinearLayoutManager(this)

        observeViewModel()
        billViewModel.getAllBills()
    }

    private fun observeViewModel() {
        billViewModel.bills.observe(this) { bills ->
            binding.recyclerViewBills.adapter = AdminBillAdapter(
                bills.toMutableList(),
                onCancel = { bill ->
                    billViewModel.cancelBill(bill.id)
                },
                onItemClick = { bill ->
                    val intent = Intent(this, AdminBillDetailActivity::class.java)
                    intent.putExtra("BILL_ID", bill.id)
                    startActivity(intent)
                }
            )
        }

        billViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        billViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        billViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}


