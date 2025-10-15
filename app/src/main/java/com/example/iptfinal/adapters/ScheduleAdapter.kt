package com.example.iptfinal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemScheduleBinding
import com.example.iptfinal.models.BabyVaccineDisplay

class ScheduleAdapter(private val scheduleList: List<BabyVaccineDisplay>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = scheduleList[position]
        val b = holder.binding

        b.tvBabyName.text = item.babyName
        b.tvVaccineName.text = "Vaccine: ${item.vaccineName}"
        b.tvDoseName.text = "Dose: ${item.doseName}"
        b.tvScheduleDate.text = "Date: ${item.scheduleDate}"
    }

    override fun getItemCount(): Int = scheduleList.size
}
