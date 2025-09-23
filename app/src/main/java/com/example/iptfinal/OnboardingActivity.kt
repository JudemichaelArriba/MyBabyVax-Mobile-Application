package com.example.iptfinal

import android.os.Bundle
import androidx.activity.enableEdgeToEdge

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.Intent

import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.iptfinal.databinding.ActivityOnboardingBinding
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    private val onboardingItems = listOf(
        OnboardingItem(R.drawable.bd1, "Welcome to My  BabyVax", "Easily keep track of your baby's vaccination schedule."),
        OnboardingItem(R.drawable.babyicon5, "Get Reminders", "Never miss a vaccination with timely notifications."),
        OnboardingItem(R.drawable.babyicon3, "Health Records", "Store and manage all your baby's health records in one place.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isOnboardingCompleted()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        setupIndicators()
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
            }
        })

        binding.buttonNext.setOnClickListener {
            val nextIndex = binding.viewPager.currentItem + 1
            if (nextIndex < onboardingItems.size) {
                binding.viewPager.currentItem = nextIndex
            } else {
                completeOnboarding()
            }
        }

        binding.buttonSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<TextView>(onboardingItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(8, 0, 8, 0) }

        binding.layoutIndicators.removeAllViews()
        for (i in indicators.indices) {
            indicators[i] = TextView(this).apply {
                text = "•"
                textSize = 35f
                setTextColor(ContextCompat.getColor(this@OnboardingActivity, R.color.whiteTransparent))
                this.layoutParams = layoutParams
            }
            binding.layoutIndicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        for (i in 0 until binding.layoutIndicators.childCount) {
            val textView = binding.layoutIndicators.getChildAt(i) as TextView
            textView.setTextColor(
                if (i == index)
                    ContextCompat.getColor(this, R.color.white)
                else
                    ContextCompat.getColor(this, R.color.whiteTransparent)
            )
        }
        binding.buttonNext.text = if (index == onboardingItems.size - 1) "Get Started" else "Next"
    }

    private fun completeOnboarding() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isOnboardingCompleted(): Boolean {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("onboarding_completed", false)
    }
}