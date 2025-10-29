package com.example.iptfinal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ItemBabyFilterBinding

class BabyFilterAdapter(
    private val babyNames: List<String>,
    private val preSelectedBaby: String?,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<BabyFilterAdapter.BabyViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION


    init {

        selectedPosition = babyNames.indexOf(preSelectedBaby)
    }


    inner class BabyViewHolder(val binding: ItemBabyFilterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyViewHolder {
        val binding = ItemBabyFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BabyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BabyViewHolder, position: Int) {
        val name = babyNames[position]
        val context = holder.binding.root.context
        val isSelected = position == selectedPosition

        holder.binding.tvBabyName.text = name
        holder.binding.tvBabyName.setTextColor(
            if (isSelected) context.getColor(android.R.color.white)
            else context.getColor(R.color.mainColor)
        )

        holder.binding.container.setBackgroundResource(
            if (isSelected) R.drawable.bg_baby_selected
            else R.drawable.bg_baby_unselected
        )

        holder.binding.container.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelect(name)
        }
    }

    override fun getItemCount() = babyNames.size
}
