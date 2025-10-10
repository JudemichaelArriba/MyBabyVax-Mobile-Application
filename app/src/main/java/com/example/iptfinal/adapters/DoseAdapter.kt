package com.example.iptfinal.adapters

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemDoseBinding
import com.example.iptfinal.models.Dose
import java.text.SimpleDateFormat
import java.util.*

class DoseAdapter(
    private val context: Context,
    private val doses: List<Dose>
) : RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {


    private val checkedStates = BooleanArray(doses.size) { false }

    inner class DoseViewHolder(val binding: ItemDoseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val binding = ItemDoseBinding.inflate(LayoutInflater.from(context), parent, false)
        return DoseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val dose = doses[position]
        val binding = holder.binding

        binding.tvDoseName.text = dose.name ?: "Dose ${position + 1}"

        val intervalValue = dose.intervalNumber ?: 0
        val intervalUnit = dose.intervalUnit ?: "days"
        binding.tvInterval.text = "Interval: $intervalValue $intervalUnit"

        binding.cbDose.setOnCheckedChangeListener(null)
        binding.cbDose.isChecked = checkedStates[position]
        binding.tvLastDate.text = if (checkedStates[position]) "Last given: —" else "Last given: —"

        binding.cbDose.setOnCheckedChangeListener { _, isChecked ->
            checkedStates[position] = isChecked
            if (isChecked) {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val date = Calendar.getInstance()
                        date.set(year, month, dayOfMonth)
                        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        binding.tvLastDate.text = "Last given: ${formatter.format(date.time)}"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } else {
                binding.tvLastDate.text = "Last given: —"
            }
        }
    }

    override fun getItemCount(): Int = doses.size


    fun setAllChecked(isChecked: Boolean) {
        for (i in checkedStates.indices) {
            checkedStates[i] = isChecked
        }
        notifyDataSetChanged()
    }
}
