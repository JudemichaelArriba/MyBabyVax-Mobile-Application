package com.example.iptfinal.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemScheduleBinding
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.pages.ScheduleInfoPage

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

        b.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ScheduleInfoPage::class.java).apply {
                putExtra("babyId", item.babyId)
                putExtra("babyName", item.babyName)
                putExtra("vaccineName", item.vaccineName)
                putExtra("doseName", item.doseName)
                putExtra("scheduleDate", item.scheduleDate)
                putExtra("vaccineType", item.vaccineType)
                putExtra("route", item.route)
                putExtra("description", item.description)
                putExtra("sideEffects", item.sideEffects)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = scheduleList.size
}
