package com.example.iptfinal.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.VaccineAdapter
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.components.bottomNav
import com.example.iptfinal.databinding.ActivitySelectVaccinePageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.*
import com.example.iptfinal.services.DatabaseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class select_vaccinePage : AppCompatActivity() {

    private lateinit var binding: ActivitySelectVaccinePageBinding
    private val databaseService = DatabaseService()
    private lateinit var vaccineAdapter: VaccineAdapter

    private var fullName: String? = null
    private var gender: String? = null
    private var dateOfBirth: String? = null
    private var birthPlace: String? = null
    private var weight: String? = null
    private var height: String? = null
    private var bloodType: String? = null
    private var babyImageUriString: String? = null
    private var babyImageUri: Uri? = null
    private var selectedVaccines = listOf<Vaccine>()
    private var doseMap = HashMap<String, List<Dose>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectVaccinePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fullName = intent.getStringExtra("fullName")
        gender = intent.getStringExtra("gender")
        dateOfBirth = intent.getStringExtra("dateOfBirth")
        birthPlace = intent.getStringExtra("birthPlace")
        weight = intent.getStringExtra("weight")
        height = intent.getStringExtra("height")
        bloodType = intent.getStringExtra("bloodType")
        babyImageUriString = intent.getStringExtra("imageUri")
        babyImageUri = babyImageUriString?.let { Uri.parse(it) }

        setupUI()
        loadVaccinesCoroutine()

        binding.btnSave.setOnClickListener {
            saveBabyWithSchedules()
        }
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        binding.recyclerViewVaccines.layoutManager = LinearLayoutManager(this)
    }

    private fun loadVaccinesCoroutine() {
        if (dateOfBirth.isNullOrEmpty()) return
        val ageInMonths = calculateAgeInMonths(dateOfBirth!!)
        binding.progressBarLoading.visibility = android.view.View.VISIBLE
        binding.recyclerViewVaccines.visibility = android.view.View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val vaccines = fetchVaccinesSuspend()
                val filteredVaccines = vaccines.filter { vaccine ->
                    val eligibleAge = vaccine.eligibleAge ?: 0
                    val ageUnit = vaccine.ageUnit ?: "months"
                    when (ageUnit.lowercase(Locale.getDefault())) {
                        "days" -> ageInMonths * 30 >= eligibleAge
                        "months" -> ageInMonths >= eligibleAge
                        "years" -> ageInMonths >= eligibleAge * 12
                        else -> true
                    }
                }

                val dosesMap = mutableMapOf<String, List<Dose>>()
                coroutineScope {
                    filteredVaccines.map { vaccine ->
                        async {
                            val doses = fetchDosesSuspend(vaccine.id ?: "")
                            dosesMap[vaccine.id ?: ""] = doses
                        }
                    }.awaitAll()
                }

                withContext(Dispatchers.Main) {
                    doseMap = HashMap(dosesMap)
                    selectedVaccines = filteredVaccines
                    vaccineAdapter =
                        VaccineAdapter(this@select_vaccinePage, filteredVaccines, dosesMap)
                    binding.recyclerViewVaccines.adapter = vaccineAdapter
                    binding.progressBarLoading.visibility = android.view.View.GONE
                    binding.recyclerViewVaccines.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("SelectVaccine", "Error loading vaccines: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.progressBarLoading.visibility = android.view.View.GONE
                }
            }
        }
    }

    private suspend fun fetchVaccinesSuspend(): List<Vaccine> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchVaccines(object : InterfaceClass.VaccineCallback {
                override fun onVaccinesLoaded(vaccines: List<Vaccine>) {
                    cont.resume(vaccines) {}
                }

                override fun onError(message: String) {
                    cont.resumeWith(Result.failure(Exception(message)))
                }
            })
        }

    private suspend fun fetchDosesSuspend(vaccineId: String): List<Dose> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchDoses(vaccineId, object : InterfaceClass.DoseCallback {
                override fun onDosesLoaded(doses: List<Dose>) {
                    cont.resume(doses) {}
                }

                override fun onError(message: String) {
                    cont.resumeWith(Result.failure(Exception(message)))
                }
            })
        }

    private fun calculateAgeInMonths(dob: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = sdf.parse(dob)
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance()
            birthCal.time = birthDate!!
            val years = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            val months = today.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
            years * 12 + months
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun saveBabyWithSchedules() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.loadingOverlay.visibility = View.VISIBLE
        val baby = Baby(
            fullName = fullName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            birthPlace = birthPlace,
            weightAtBirth = weight?.toDoubleOrNull(),
            heightAtBirth = height?.toDoubleOrNull(),
            bloodType = bloodType,
            profileImageUrl = babyImageUriString
        )

        val schedules = selectedVaccines.map { vaccine ->
            val doses = doseMap[vaccine.id] ?: emptyList()
            var lastDate = Calendar.getInstance()

            lastDate = getNextWeekday(lastDate, vaccine.schedule)

            val doseSchedules = doses.map { dose ->
                val intervalNum = dose.intervalNumber ?: 0
                val intervalUnit = dose.intervalUnit ?: "days"

                lastDate = calculateNextDoseDate(lastDate, intervalNum, intervalUnit)

                lastDate = getNextWeekday(lastDate, vaccine.schedule)

                val intervalText = "$intervalNum $intervalUnit"
                BabyDoseSchedule(dose.name, intervalText, formatCalendarDate(lastDate))
            }

            BabyVaccineSchedule(
                vaccineName = vaccine.name,
                vaccineType = vaccine.type,
                description = vaccine.description,
                route = vaccine.route,
                sideEffects = vaccine.sideEffects,
                lastGiven = getTodayDate(),
                doses = if (vaccine.hasDosage) doseSchedules else null
            )
        }

        databaseService.addBabyWithSchedule(
            userId,
            baby,
            schedules,
            object : InterfaceClass.StatusCallback {
                override fun onSuccess(message: String) {
                    binding.loadingOverlay.visibility = View.GONE
                    Log.d("AddBaby", message)
                    DialogHelper.showSuccess(
                        this@select_vaccinePage,
                        "Successfully Saved",
                        "The Baby info is Successfully saved"
                    ) {

                        finish()
                    }


                }

                override fun onError(error: String) {
                    Log.e("AddBaby", error ?: "")
                }
            })
    }


    private fun calculateNextDoseDate(
        baseDate: Calendar,
        intervalNumber: Int,
        intervalUnit: String
    ): Calendar {
        val calendar = baseDate.clone() as Calendar
        when (intervalUnit.lowercase()) {
            "days" -> calendar.add(Calendar.DAY_OF_YEAR, intervalNumber)
            "weeks" -> calendar.add(Calendar.WEEK_OF_YEAR, intervalNumber)
            "months" -> calendar.add(Calendar.MONTH, intervalNumber)
            "years" -> calendar.add(Calendar.YEAR, intervalNumber)
        }
        return calendar
    }

    private fun getNextWeekday(baseDate: Calendar, weekday: String?): Calendar {
        if (weekday.isNullOrEmpty()) return baseDate
        val calendar = baseDate.clone() as Calendar
        val targetDay = when (weekday.lowercase()) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> return calendar
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar
    }


    private fun formatCalendarDate(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }


}
