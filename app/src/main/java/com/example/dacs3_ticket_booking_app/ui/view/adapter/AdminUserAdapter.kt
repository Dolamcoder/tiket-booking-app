package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.User
import com.example.dacs3_ticket_booking_app.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private val items: MutableList<User>,
    private val onEdit: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvFullName.text = user.fullName.ifEmpty { "N/A" }
            binding.tvEmail.text = user.email
            binding.tvRole.text = "Role: ${if (user.role == "admin") "Admin" else "User"}"
            binding.tvMoney.text = "Money: ${String.format("%.0f", user.accumulatedMoney)}"

            // Load avatar
            if (user.avatar.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.avatar)
                    .into(binding.ivUserAvatar)
            }

            binding.btnEdit.setOnClickListener { onEdit(user) }
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete User")
                    .setMessage("Delete \"${user.fullName}\"?")
                    .setPositiveButton("Delete") { _, _ -> onDelete(user) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}



