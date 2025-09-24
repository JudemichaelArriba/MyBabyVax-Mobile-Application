package com.example.iptfinal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.databinding.ItemOnboardingBinding



class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            onboardingTitle.text = item.title
            onboardingDescription.text = item.description
            if (item.image != null) {
                onboardingImage.setImageResource(item.image)
            } else {
                onboardingImage.setImageResource(android.R.color.transparent)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}