package com.example.iptfinal.adapters

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemVaccineBinding
import com.example.iptfinal.models.Vaccine
import com.example.iptfinal.models.Dose
import java.text.SimpleDateFormat
import java.util.*

class VaccineAdapter(
    private val context: Context,
    private val vaccines: List<Vaccine>,
    private val doseMap: Map<String, List<Dose>>
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

        val doses = doseMap[vaccine.id] ?: emptyList()
        val doseAdapter = DoseAdapter(context, doses)
        binding.recyclerViewDoses.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDoses.adapter = doseAdapter

        var isExpanded = false
        binding.ivExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.recyclerViewDoses.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.divider.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpand.rotation = if (isExpanded) 180f else 0f
        }


        binding.cbVaccine.setOnCheckedChangeListener(null)
        binding.cbVaccine.isChecked = false


        binding.cbVaccine.setOnCheckedChangeListener { _, isChecked ->
            doseAdapter.setAllChecked(isChecked)

            if (isChecked) {

                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)
                        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        val formattedDate = formatter.format(selectedDate.time)


                        binding.tvVaccineType.text = "Last given: $formattedDate"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } else {

                binding.tvVaccineType.text = "Type: ${vaccine.type ?: "Unknown"}"
            }
        }
    }

    override fun getItemCount(): Int = vaccines.size
}
