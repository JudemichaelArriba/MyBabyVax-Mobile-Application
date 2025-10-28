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
            if (dose.isCompleted && !dose.date.isNullOrEmpty()) "Last given: ${dose.date}"
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
                        dose.date = formattedDate
                        dose.isVisible = false


                        if (position + 1 < doseSchedules.size) {
                            doseSchedules[position + 1].isVisible = true
                        }


                        notifyDataSetChanged()
                        onDoseStatusChanged(doseSchedules)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } else {

                dose.isCompleted = false
                dose.date = ""

                dose.isVisible = true

                for (i in position + 1 until doseSchedules.size) {
                    doseSchedules[i].isVisible = false

                }

                notifyDataSetChanged()
                onDoseStatusChanged(doseSchedules)
            }
        }
    }

    override fun getItemCount(): Int = doseSchedules.size


    fun setAllChecked(isChecked: Boolean) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (isChecked) {
            for (i in doseSchedules.indices) {
                val d = doseSchedules[i]
                d.isCompleted = true
                if (d.date.isNullOrEmpty()) {
                    d.date = today
                }
                d.isVisible = true
            }
        } else {
            for (i in doseSchedules.indices) {
                val d = doseSchedules[i]
                d.isCompleted = false
                d.date = ""
                d.isVisible = (i == 0)
            }
        }

        notifyDataSetChanged()
        onDoseStatusChanged(doseSchedules)
    }
}
