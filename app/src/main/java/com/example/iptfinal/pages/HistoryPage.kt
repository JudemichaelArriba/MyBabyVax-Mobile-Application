package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptfinal.adapters.AdapterHistory
import com.example.iptfinal.adapters.BabyFilterAdapter
import com.example.iptfinal.databinding.ActivityHistoryPageBinding
import com.example.iptfinal.models.BabyVaccineHistory
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.iptfinal.R

class HistoryPage : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryPageBinding
    private val databaseService = DatabaseService()
    private val historyList = mutableListOf<BabyVaccineHistory>()
    private val allHistoryList = mutableListOf<BabyVaccineHistory>()
    private var selectedBaby: String? = "All Babies"
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
        setUpSwipeRefresh()

        binding.filter.setOnClickListener {
            showFilterBottomSheet()
        }
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
                allHistoryList.clear()

                historyMap.values.forEach { list ->
                    allHistoryList.addAll(list)
                }

                historyList.addAll(allHistoryList)
                historyAdapter.notifyDataSetChanged()
                binding.loading.visibility = android.view.View.GONE
                binding.babyRecyclerView.visibility = android.view.View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
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

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filter, null)
        dialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.babyRecyclerView)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)

        val babyNames = mutableListOf("All Babies")
        babyNames.addAll(allHistoryList.mapNotNull { it.babyFullName }.distinct())

        val adapter = BabyFilterAdapter(babyNames, selectedBaby) { selected ->
            selectedBaby = selected
        }


        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        btnApply.setOnClickListener {
            val filteredList = if (selectedBaby == null || selectedBaby == "All Babies") {
                allHistoryList
            } else {
                allHistoryList.filter { it.babyFullName == selectedBaby }
            }


            historyAdapter.updateList(filteredList)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun setUpSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.mainColor))
        binding.swipeRefresh.setOnRefreshListener {
            fetchHistory()
        }
    }


}
