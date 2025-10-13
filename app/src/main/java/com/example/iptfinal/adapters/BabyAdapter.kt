package com.example.iptfinal.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ItemBabyBinding
import com.example.iptfinal.models.Baby
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat

class BabyAdapter : RecyclerView.Adapter<BabyAdapter.BabyViewHolder>() {

    private val babies = mutableListOf<Baby>()

    inner class BabyViewHolder(val binding: ItemBabyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyViewHolder {
        val binding = ItemBabyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BabyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BabyViewHolder, position: Int) {
        val baby = babies[position]
        with(holder.binding) {
            tvBabyName.text = baby.fullName ?: "No Name"
            tvBabyAge.text = calculateAge(baby.dateOfBirth)
            tvBirthWeight.text = "${baby.weightAtBirth ?: 0.0}kg"
            tvBirthHeight.text = "${baby.heightAtBirth ?: 0}cm"


            if (!baby.profileImageUrl.isNullOrEmpty()) {
                try {

                    val imageBytes = Base64.decode(baby.profileImageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    Glide.with(holder.binding.profileImage.context)
                        .load(bitmap)
                        .circleCrop()
                        .into(holder.binding.profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    holder.binding.profileImage.setImageResource(android.R.color.darker_gray)
                }
            } else {
                holder.binding.profileImage.setImageResource(android.R.color.darker_gray)
            }

            when (baby.gender?.lowercase()) {
                "male" -> {
                    genderIcon.setImageResource(R.drawable.ic_male_icon)
                    genderIcon.setColorFilter(null)
                }
                "female" -> {
                    genderIcon.setImageResource(R.drawable.ic_female_icon)
                    genderIcon.setColorFilter(
                        ContextCompat.getColor(genderIcon.context, R.color.pink),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                else -> {
                    genderIcon.setImageResource(android.R.color.transparent)
                }
            }



            btnMoreOptions.setOnClickListener {

            }
        }
    }

    override fun getItemCount(): Int = babies.size


    fun submitList(newList: List<Baby>) {
        babies.clear()
        babies.addAll(newList)
        notifyDataSetChanged()
    }

    private fun calculateAge(dob: String?): String {
        if (dob.isNullOrEmpty()) return ""

        val dobParts = dob.split("-")
        if (dobParts.size != 3) return ""
        val year = dobParts[0].toIntOrNull() ?: return ""
        val month = dobParts[1].toIntOrNull() ?: return ""
        val day = dobParts[2].toIntOrNull() ?: return ""
        val today = java.util.Calendar.getInstance()
        var ageYears = today.get(java.util.Calendar.YEAR) - year
        var ageMonths = today.get(java.util.Calendar.MONTH) + 1 - month
        if (ageMonths < 0) {
            ageYears--
            ageMonths += 12
        }
        return if (ageYears == 0) "$ageMonths months old" else "$ageYears years old"
    }
}
