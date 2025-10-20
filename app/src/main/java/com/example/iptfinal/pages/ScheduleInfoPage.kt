package com.example.iptfinal.pages

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.R
import com.example.iptfinal.adapters.BabyAdapter
import com.example.iptfinal.databinding.ActivityScheduleInfoPageBinding
import com.example.iptfinal.models.Baby
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScheduleInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleInfoPageBinding
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.mainColor)


        val babyId = intent.getStringExtra("babyId")
        val vaccineName = intent.getStringExtra("vaccineName")
        val doseName = intent.getStringExtra("doseName")
        val scheduleDate = intent.getStringExtra("scheduleDate")
        val vaccineType = intent.getStringExtra("vaccineType")
        val route = intent.getStringExtra("route")
        val description = intent.getStringExtra("description")
        val sideEffects = intent.getStringExtra("sideEffects")


        binding.vaccineName.text = vaccineName
        binding.doseName.text = doseName
        binding.vaccineType.text = vaccineType
        binding.route.text = route
        binding.scheduleDate.text = scheduleDate
        binding.description.text = description
        binding.sideEffects.text = sideEffects


        babyAdapter = BabyAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ScheduleInfoPage)
            adapter = babyAdapter
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && babyId != null) {
            fetchBabyByIdCoroutine(babyId)
        }
    }

    private fun fetchBabyByIdCoroutine(babyId: String) {
        lifecycleScope.launchWhenStarted {
            binding.loading.visibility = View.VISIBLE
            try {
                val baby = withContext(Dispatchers.IO) {
                    fetchBabySuspend(babyId)
                }

                binding.loading.visibility = View.GONE
                babyAdapter.submitList(listOf(baby))
                binding.recyclerView.visibility = View.VISIBLE

            } catch (e: Exception) {
                binding.loading.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
            }
        }
    }

    /**
     * Converts the callback based fetchBabyById() to coroutine compatible.
     * using coroutine for smooth ui
     */
    private suspend fun fetchBabySuspend(babyId: String): Baby =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
                override fun onBabyLoaded(baby: Baby) {
                    cont.resume(baby)
                }

                override fun onError(message: String) {
                    cont.resumeWithException(Exception(message))
                }
            })
        }
}
