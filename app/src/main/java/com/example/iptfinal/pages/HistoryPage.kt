package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.AdapterHistory
import com.example.iptfinal.databinding.ActivityHistoryPageBinding
import com.example.iptfinal.models.BabyVaccineHistory
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HistoryPage : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryPageBinding
    private val databaseService = DatabaseService()
    private val historyList = mutableListOf<BabyVaccineHistory>()
    private lateinit var historyAdapter: AdapterHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        fetchHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = AdapterHistory(historyList)
        binding.babyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryPage)
            adapter = historyAdapter
        }
    }

    private fun fetchHistory() {
        val userId = SessionManager(this).getUser().uid
        binding.loading.visibility = android.view.View.VISIBLE
        binding.babyRecyclerView.visibility = android.view.View.GONE
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val historyMap = fetchHistoryAsync(userId)
                historyList.clear()
                historyMap.values.forEach { list ->
                    historyList.addAll(list)
                }

                historyAdapter.notifyDataSetChanged()
                binding.loading.visibility = android.view.View.GONE
                binding.babyRecyclerView.visibility = android.view.View.VISIBLE
            } catch (e: Exception) {
                Log.e("HistoryPage", "Error fetching history: ${e.message}")
                binding.loading.visibility = android.view.View.GONE
            }
        }
    }


    private suspend fun fetchHistoryAsync(userId: String): Map<String, List<BabyVaccineHistory>> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabiesHistoryForUser(
                userId,
                callback = { historyMap ->
                    cont.resume(historyMap)
                },
                errorCallback = { error ->
                    cont.resumeWithException(Exception(error))
                }
            )
        }
}
