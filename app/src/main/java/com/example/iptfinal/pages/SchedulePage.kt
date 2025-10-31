package com.example.iptfinal.pages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.ScheduleAdapter
import com.example.iptfinal.databinding.ActivitySchedulePageBinding
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

        binding.backButton.setOnClickListener {
            finish()
        }
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

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                launch(Dispatchers.Main) { binding.loading.visibility = View.VISIBLE }

                val allSchedules = fetchSchedules(currentUserId)


                val notCompletedSchedules = allSchedules.filter { !it.isCompleted }

                launch(Dispatchers.Main) {
                    if (notCompletedSchedules.isEmpty()) {
                        Toast.makeText(
                            this@SchedulePage,
                            "All vaccines are completed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    scheduleAdapter = ScheduleAdapter(notCompletedSchedules.toMutableList())
                    binding.recyclerView.adapter = scheduleAdapter
                    binding.loading.visibility = View.GONE
                }

            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(
                        this@SchedulePage,
                        e.message ?: "Error loading schedules",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun fetchSchedules(userId: String): List<BabyVaccineDisplay> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabyVaccineSchedules(
                userId,
                object : InterfaceClass.BabyVaccineDisplayCallback {
                    override fun onSchedulesLoaded(schedules: List<BabyVaccineDisplay>) {
                        cont.resume(schedules)
                    }

                    override fun onError(error: String) {
                        cont.resumeWithException(Exception(error))
                    }
                }
            )
        }
}
