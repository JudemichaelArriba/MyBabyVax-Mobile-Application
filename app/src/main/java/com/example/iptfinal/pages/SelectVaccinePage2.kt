package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.VaccineAdapter
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.databinding.ActivitySelectVaccinePageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.BabyDoseSchedule
import com.example.iptfinal.models.Dose
import com.example.iptfinal.models.Vaccine
import com.example.iptfinal.services.DatabaseService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SelectVaccinePage2 : AppCompatActivity() {

    private lateinit var binding: ActivitySelectVaccinePageBinding
    private val databaseService = DatabaseService()
    private lateinit var vaccineAdapter: VaccineAdapter

    private var babyId: String? = null
    private var dateOfBirth: String? = null
    private var selectedVaccines = listOf<Vaccine>()
    private var doseMap = HashMap<String, List<Dose>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectVaccinePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        babyId = intent.getStringExtra("baby_id")
        dateOfBirth = intent.getStringExtra("dateOfBirth")

        setupUI()
        loadVaccinesNotScheduled()

        binding.btnSave.setOnClickListener {
            saveSelectedSchedules()
        }
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        binding.recyclerViewVaccines.layoutManager = LinearLayoutManager(this)
    }

    private fun loadVaccinesNotScheduled() {
        val babyIdSafe = babyId ?: return
        binding.progressBarLoading.visibility = View.VISIBLE
        binding.recyclerViewVaccines.visibility = View.GONE

        databaseService.fetchVaccinesNotInBabySchedule(
            babyIdSafe,
            object : InterfaceClass.VaccineCallback {
                override fun onVaccinesLoaded(vaccines: List<Vaccine>) {
                    if (vaccines.isEmpty()) {
                        binding.progressBarLoading.visibility = View.GONE
                        return
                    }

                    lifecycleScope.launch {
                        try {
                            val dosesMap = mutableMapOf<String, List<Dose>>()
                            coroutineScope {
                                vaccines.map { vaccine ->
                                    async {
                                        val doses = fetchDoses(vaccine.id ?: "")
                                        dosesMap[vaccine.id ?: ""] = doses
                                    }
                                }.awaitAll()
                            }

                            doseMap = HashMap(dosesMap)
                            selectedVaccines = vaccines

                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val babyDoseMap = mutableMapOf<String, MutableList<BabyDoseSchedule>>()
                            val birthCal = Calendar.getInstance()
                            if (dateOfBirth != null) {
                                val sdfBirth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                birthCal.time = sdfBirth.parse(dateOfBirth!!) ?: Date()
                            }

                            for (vaccine in vaccines) {
                                val doses = dosesMap[vaccine.id] ?: emptyList()
                                val babyDoseList = doses.map { dose ->
                                    val doseCal = birthCal.clone() as Calendar
                                    when (dose.intervalUnit?.lowercase()) {
                                        "days" -> doseCal.add(
                                            Calendar.DAY_OF_YEAR,
                                            (dose.intervalNumber ?: 0.0).toInt()
                                        )

                                        "weeks" -> doseCal.add(
                                            Calendar.DAY_OF_YEAR,
                                            ((dose.intervalNumber ?: 0.0) * 7).toInt()
                                        )

                                        "months" -> {
                                            val months = (dose.intervalNumber ?: 0.0).toInt()
                                            val halfMonth = if ((dose.intervalNumber
                                                    ?: 0.0) % 1 >= 0.5
                                            ) 15 else 0
                                            doseCal.add(Calendar.MONTH, months)
                                            doseCal.add(Calendar.DAY_OF_YEAR, halfMonth)
                                        }

                                        "years" -> doseCal.add(
                                            Calendar.YEAR,
                                            (dose.intervalNumber ?: 0.0).toInt()
                                        )
                                    }

                                    BabyDoseSchedule(
                                        doseName = dose.name,
                                        interval = "${dose.intervalNumber} ${dose.intervalUnit}",
                                        date = sdf.format(doseCal.time),
                                        isVisible = false,
                                        isCompleted = false
                                    )
                                }.toMutableList()
                                if (babyDoseList.isNotEmpty()) babyDoseList[0].isVisible = true
                                babyDoseMap[vaccine.id ?: ""] = babyDoseList
                            }

                            vaccineAdapter =
                                VaccineAdapter(this@SelectVaccinePage2, vaccines, babyDoseMap)
                            binding.recyclerViewVaccines.apply {
                                layoutManager = LinearLayoutManager(this@SelectVaccinePage2)
                                adapter = vaccineAdapter
                                visibility = View.VISIBLE
                            }
                            binding.progressBarLoading.visibility = View.GONE
                        } catch (e: Exception) {
                            Log.e("SelectVaccinePage2", "Error loading doses: ${e.message}")
                            binding.progressBarLoading.visibility = View.GONE
                        }
                    }
                }

                override fun onError(message: String) {
                    Log.e("SelectVaccinePage2", "Error fetching vaccines: $message")
                    binding.progressBarLoading.visibility = View.GONE
                }
            })
    }

    private suspend fun fetchDoses(vaccineId: String): List<Dose> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            databaseService.fetchDoses(vaccineId, object : InterfaceClass.DoseCallback {
                override fun onDosesLoaded(doses: List<Dose>) {
                    cont.resume(doses) {}
                }

                override fun onError(message: String) {
                    cont.resumeWith(Result.failure(Exception(message)))
                }
            })
        }

    private fun saveSelectedSchedules() {
        val babyIdSafe = babyId ?: return
        binding.loadingOverlay.visibility = View.VISIBLE

        val updatedDoseMap = vaccineAdapter.getUpdatedVaccineSchedules()
        val newSchedules = selectedVaccines.map { vaccine ->
            val doseSchedules = updatedDoseMap[vaccine.id] ?: mutableListOf()
            com.example.iptfinal.models.BabyVaccineSchedule(
                vaccineName = vaccine.name,
                vaccineType = vaccine.type,
                description = vaccine.description,
                route = vaccine.route,
                sideEffects = vaccine.sideEffects,
                lastGiven = null,
                doses = if (vaccine.hasDosage) doseSchedules else null
            )
        }

        databaseService.addSchedulesToExistingBaby(
            babyIdSafe,
            newSchedules,
            object : InterfaceClass.StatusCallback {
                override fun onSuccess(message: String) {
                    binding.loadingOverlay.visibility = View.GONE
                    DialogHelper.showSuccess(
                        this@SelectVaccinePage2,
                        "Success",
                        "New schedules have been added successfully."
                    ) { finish() }
                }

                override fun onError(error: String) {
                    binding.loadingOverlay.visibility = View.GONE
                    DialogHelper.showError(
                        this@SelectVaccinePage2,
                        "Error",
                        "Failed to add schedules: $error"
                    )
                }
            })
    }
}
