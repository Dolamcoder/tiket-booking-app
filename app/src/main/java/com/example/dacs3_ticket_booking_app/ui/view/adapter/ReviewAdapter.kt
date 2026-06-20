package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.R
import com.example.dacs3_ticket_booking_app.data.model.Review
import com.example.dacs3_ticket_booking_app.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.tvUserName.text = review.userName
        holder.binding.tvComment.text = review.comment
        holder.binding.ratingBar.rating = review.rating
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.tvDate.text = sdf.format(Date(review.timestamp))

        if (review.userAvatar.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.userAvatar)
                .placeholder(R.drawable.back_dark) // Reusing existing placeholder
                .error(R.drawable.back_dark)
                .into(holder.binding.ivUserAvatar)
        } else {
            holder.binding.ivUserAvatar.setImageResource(R.drawable.back_dark)
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}
