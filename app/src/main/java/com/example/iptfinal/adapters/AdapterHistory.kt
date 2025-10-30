package com.example.iptfinal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemHistoryBinding
import com.example.iptfinal.models.BabyVaccineHistory
import com.example.iptfinal.pages.HistoryInfoPage

class AdapterHistory(
    private val historyList: MutableList<BabyVaccineHistory>
) : RecyclerView.Adapter<AdapterHistory.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        holder.binding.tvBabyName.text = history.babyFullName ?: "Unknown"
        holder.binding.tvVaccineName.text = history.vaccineName ?: "Unknown Vaccine"
        holder.binding.tvDoseName.text = history.doseName ?: "Unknown Dose"
        holder.binding.tvScheduleDate.text = history.date ?: "No Date"



        holder.itemView.setOnClickListener { view ->
            val context = view.context
            val intent = android.content.Intent(context, HistoryInfoPage::class.java)
            intent.putExtra("babyFullName", history.babyFullName)
            intent.putExtra("babyGender", history.babyGender)
            intent.putExtra("babyDateOfBirth", history.babyDateOfBirth)
            intent.putExtra("babyBloodType", history.babyBloodType)
            intent.putExtra("vaccineName", history.vaccineName)
            intent.putExtra("doseName", history.doseName)
            intent.putExtra("date", history.date)
            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<BabyVaccineHistory>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = historyList.size
}
