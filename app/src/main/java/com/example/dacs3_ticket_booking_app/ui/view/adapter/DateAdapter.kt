package com.example.dacs3_ticket_booking_app.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.dacs3_ticket_booking_app.R
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3_ticket_booking_app.databinding.ItemDateBinding

class DateAdapter(private val timeSlots: List<String> ): RecyclerView.Adapter<DateAdapter.DateViewHolder>(){
    private var selectedPosition=-1
    private var lastSelectedPosition=-1
    inner class DateViewHolder(private val binding: ItemDateBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(date: String, position: Int){
            val dateParts=date.split("/")
            if(dateParts.size==3){
                binding.dayTxt.text=dateParts[0]
                binding.dayMonthTxt.text=dateParts[1]+" "+dateParts[2]
                if(selectedPosition==position){
                    binding.mainLayout.setBackgroundResource(R.drawable.orange_bg)
                    binding.dayTxt.setTextColor(binding.root.context.getColor(R.color.black))
                    binding.dayMonthTxt.setTextColor(binding.root.context.getColor(R.color.black))
                }else{
                    binding.mainLayout.setBackgroundResource(R.drawable.light_black_bg)
                    binding.dayTxt.setTextColor(binding.root.context.getColor(R.color.white))
                    binding.dayMonthTxt.setTextColor(binding.root.context.getColor(R.color.white))
                }
                binding.root.setOnClickListener {
                    if(position!= RecyclerView.NO_POSITION){
                        lastSelectedPosition=selectedPosition
                        selectedPosition=position
                        notifyItemChanged(lastSelectedPosition)
                        notifyItemChanged(selectedPosition)
                    }
                }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DateAdapter.DateViewHolder {
       return DateViewHolder(
           ItemDateBinding.inflate(
               LayoutInflater.from(parent.context),
               parent,
               false
           )
       )
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
       holder.bind(timeSlots[position], position)
    }

    override fun getItemCount(): Int =timeSlots.size

}