package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminTicketListBinding
import com.example.dacs3_ticket_booking_app.ui.view.adapter.AdminTicketAdapter
import com.example.dacs3_ticket_booking_app.ui.viewmodel.TicketViewModel

class AdminTicketListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminTicketListBinding
    private lateinit var ticketViewModel: TicketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTicketListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ticketViewModel = ViewModelProvider(this).get(TicketViewModel::class.java)

        binding.backBtn.setOnClickListener { finish() }
        binding.recyclerViewTickets.layoutManager = LinearLayoutManager(this)

        observeViewModel()
        ticketViewModel.getAllTickets()
    }

    private fun observeViewModel() {
        ticketViewModel.tickets.observe(this) { tickets ->
            binding.recyclerViewTickets.adapter = AdminTicketAdapter(
                tickets.toMutableList(),
                onCancel = { ticket ->
                    ticketViewModel.cancelTicket(ticket.id)
                }
            )
        }

        ticketViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        ticketViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        ticketViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
