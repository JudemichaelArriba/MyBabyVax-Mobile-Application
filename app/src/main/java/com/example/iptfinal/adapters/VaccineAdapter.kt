package com.example.iptfinal.adapters

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemVaccineBinding
import com.example.iptfinal.models.BabyDoseSchedule
import com.example.iptfinal.models.Vaccine
import java.text.SimpleDateFormat
import java.util.*

class VaccineAdapter(
    private val context: Context,
    private val vaccines: List<Vaccine>,
    private val babyDoseMap: MutableMap<String, MutableList<BabyDoseSchedule>>
) : RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {

    inner class VaccineViewHolder(val binding: ItemVaccineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val binding = ItemVaccineBinding.inflate(LayoutInflater.from(context), parent, false)
        return VaccineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val vaccine = vaccines[position]
        val binding = holder.binding

        binding.tvVaccineName.text = vaccine.name ?: "Unnamed Vaccine"
        binding.tvVaccineType.text = "Type: ${vaccine.type ?: "Unknown"}"

        val doseSchedules = babyDoseMap[vaccine.id]?.toMutableList() ?: mutableListOf()

        val doseAdapter = DoseAdapter(context, doseSchedules) { updatedList ->
            babyDoseMap[vaccine.id ?: ""] = updatedList.toMutableList()
        }

        binding.recyclerViewDoses.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDoses.adapter = doseAdapter


        binding.cbVaccine.setOnCheckedChangeListener(null)
        binding.cbVaccine.isChecked = doseSchedules.all { it.isCompleted }

        binding.cbVaccine.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = formatter.format(selectedDate.time)


                        doseAdapter.setAllChecked(true)

                        val list = babyDoseMap[vaccine.id ?: ""] ?: mutableListOf()
                        for (d in list) {
                            if (d.date.isNullOrEmpty()) d.date = formattedDate
                        }

                        babyDoseMap[vaccine.id ?: ""] = list
                        doseAdapter.notifyDataSetChanged()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } else {

                doseAdapter.setAllChecked(false)
            }
        }

        var isExpanded = false
        binding.ivExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.recyclerViewDoses.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.divider.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpand.rotation = if (isExpanded) 180f else 0f
        }
    }

    override fun getItemCount(): Int = vaccines.size

    fun getUpdatedVaccineSchedules(): Map<String, MutableList<BabyDoseSchedule>> {
        return babyDoseMap
    }
}
