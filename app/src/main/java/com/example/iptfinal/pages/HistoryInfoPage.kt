package com.example.iptfinal.pages

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.databinding.ActivityHistoryInfoPageBinding

class HistoryInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryInfoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        val babyFullName = intent.getStringExtra("babyFullName") ?: "Unknown"
        val babyGender = intent.getStringExtra("babyGender") ?: "Unknown"
        val babyDateOfBirth = intent.getStringExtra("babyDateOfBirth") ?: "Unknown"
        val babyBloodType = intent.getStringExtra("babyBloodType") ?: "Unknown"
        val vaccineName = intent.getStringExtra("vaccineName") ?: "Unknown Vaccine"
        val doseName = intent.getStringExtra("doseName") ?: "Unknown Dose"
        val date = intent.getStringExtra("date") ?: "No Date"


        binding.vaccineName.text = vaccineName
        binding.doseName.text = doseName
        binding.babyNameTv.text = babyFullName
        binding.babyGenderTv.text = babyGender
        binding.babyBirthdayTv.text = babyDateOfBirth
        binding.babyBloodTypeTv.text = babyBloodType
        binding.dateTv.text = date
    }
}

