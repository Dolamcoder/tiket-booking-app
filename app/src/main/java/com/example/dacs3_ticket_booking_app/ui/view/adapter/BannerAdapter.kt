package com.example.ticketbookingapp.ui.view.adaper

import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.dacs3_ticket_booking_app.databinding.ViewholderSliderBinding
import android.content.Context
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.dacs3_ticket_booking_app.ui.view.DetailMovieActivity
import com.example.dacs3_ticket_booking_app.data.model.Banner


class BannerAdapter(
    private var sliderItems:MutableList<Banner>,
    private val viewPager2: ViewPager2

): RecyclerView.Adapter<BannerAdapter.SliderViewHolder>() {
    private var context: Context?=null
    private val runnable= Runnable{
        sliderItems.addAll(sliderItems)
        notifyDataSetChanged()
    }

    inner class SliderViewHolder(private val binding: ViewholderSliderBinding  ): RecyclerView.ViewHolder(binding.root) {
        fun bind(Banner: Banner) {
            context?.let{
                Glide.with(it)
                    .load(Banner.image)
                    .apply(
                        RequestOptions().transform(CenterCrop(), RoundedCorners(60))
                    )
                    .into(binding.imageSlider)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BannerAdapter.SliderViewHolder {
        context=parent.context
        val binding = ViewholderSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerAdapter.SliderViewHolder, position: Int) {
        holder.bind(sliderItems[position])
        if (position == sliderItems.size - 2) {
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int =sliderItems.size
}