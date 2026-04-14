package com.example.dacs3_ticket_booking_app.ui.view.adapter

    import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.Cast
import com.example.dacs3_ticket_booking_app.databinding.ItemCastSelectionBinding

class CastSelectionAdapter(
    private val items: List<Cast>,
    private val onSelectionChanged: (Cast, Boolean) -> Unit
) : RecyclerView.Adapter<CastSelectionAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

    inner class ViewHolder(private val binding: ItemCastSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cast: Cast) {
            binding.tvCastName.text = cast.name

            if (cast.images.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(cast.images)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.imgCastAvatar)
            }

            binding.cbSelect.isChecked = selectedItems.contains(cast.id)

            binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(cast.id)
                } else {
                    selectedItems.remove(cast.id)
                }
                onSelectionChanged(cast, isChecked)
            }

            binding.root.setOnClickListener {
                binding.cbSelect.isChecked = !binding.cbSelect.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCastSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    fun getSelectedCasts(): List<Cast> {
        return items.filter { selectedItems.contains(it.id) }
    }

    fun setSelectedCasts(casts: List<Cast>) {
        selectedItems.clear()
        selectedItems.addAll(casts.map { it.id })
        notifyDataSetChanged()
    }
}

