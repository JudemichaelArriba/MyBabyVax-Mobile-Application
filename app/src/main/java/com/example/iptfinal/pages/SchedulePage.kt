package com.example.iptfinal.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.ScheduleAdapter
import com.example.iptfinal.databinding.ActivitySchedulePageBinding
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class SchedulePage : AppCompatActivity() {
    private lateinit var binding: ActivitySchedulePageBinding
    private val databaseService = DatabaseService()
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadSchedules()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SchedulePage)
        }
    }

    private fun loadSchedules() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        databaseService.fetchAllBabyVaccineSchedules(
            currentUserId,
            object : InterfaceClass.BabyVaccineDisplayCallback {
                override fun onSchedulesLoaded(schedules: List<BabyVaccineDisplay>) {
                    scheduleAdapter = ScheduleAdapter(schedules)
                    binding.recyclerView.adapter = scheduleAdapter
                }

                override fun onError(error: String) {
                    Toast.makeText(this@SchedulePage, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
