package com.example.iptfinal.adapters

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemDoseBinding
import com.example.iptfinal.models.BabyDoseSchedule
import java.text.SimpleDateFormat
import java.util.*

class DoseAdapter(
    private val context: Context,
    private val doseSchedules: MutableList<BabyDoseSchedule>,
    private val onDoseStatusChanged: (List<BabyDoseSchedule>) -> Unit
) : RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    inner class DoseViewHolder(val binding: ItemDoseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val binding = ItemDoseBinding.inflate(LayoutInflater.from(context), parent, false)
        return DoseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val dose = doseSchedules[position]
        val binding = holder.binding

        binding.tvDoseName.text = dose.doseName
        binding.tvInterval.text = "Interval: ${dose.interval}"
        binding.cbDose.setOnCheckedChangeListener(null)
        binding.cbDose.isChecked = dose.isCompleted

        binding.tvLastDate.text =
            if (dose.isCompleted && dose.date!!.isNotEmpty()) "Last given: ${dose.date}"
            else "Last given: —"

        binding.cbDose.isEnabled = dose.isVisible

        binding.cbDose.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = formatter.format(selectedDate.time)

                        dose.isCompleted = true
                        dose.isVisible = false
                        dose.date = formattedDate

                        if (position + 1 < doseSchedules.size) {
                            doseSchedules[position + 1].isVisible = true
                        }

                        binding.cbDose.isEnabled = false
                        notifyDataSetChanged()


                        onDoseStatusChanged(doseSchedules)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = doseSchedules.size
}
