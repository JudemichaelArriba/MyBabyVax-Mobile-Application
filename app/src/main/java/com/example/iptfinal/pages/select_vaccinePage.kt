package com.example.iptfinal.pages

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
            saveBabyWithSchedulesCoroutine()
        }
    }


    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        binding.recyclerViewVaccines.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Loads ALL vaccines from Firebase (no filtering by eligible age)
     */
    private fun loadVaccinesCoroutine() {
        binding.progressBarLoading.visibility = View.VISIBLE
        binding.recyclerViewVaccines.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val vaccines = fetchVaccines()


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


                vaccineAdapter = VaccineAdapter(this@select_vaccinePage, vaccines, dosesMap)
                binding.recyclerViewVaccines.adapter = vaccineAdapter
                binding.progressBarLoading.visibility = View.GONE
                binding.recyclerViewVaccines.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("SelectVaccine", "Error loading vaccines: ${e.message}")
                binding.progressBarLoading.visibility = View.GONE
            }
        }
    }


    private suspend fun fetchVaccines(): List<Vaccine> = suspendCancellableCoroutine { cont ->
        databaseService.fetchVaccines(object : InterfaceClass.VaccineCallback {
            override fun onVaccinesLoaded(vaccines: List<Vaccine>) {
                cont.resume(vaccines) {}
            }

            override fun onError(message: String) {
                cont.resumeWith(Result.failure(Exception(message)))
            }
        })
    }


    private suspend fun fetchDoses(vaccineId: String): List<Dose> =
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


    /**
     * Saves baby data + automatically calculates vaccine schedule dates based on intervals
     */
    private fun saveBabyWithSchedulesCoroutine() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.loadingOverlay.visibility = View.VISIBLE

        val imageBase64 = babyImageUri?.let { uriToBase64(it) }

        val baby = Baby(
            fullName = fullName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            birthPlace = birthPlace,
            weightAtBirth = weight?.toDoubleOrNull(),
            heightAtBirth = height?.toDoubleOrNull(),
            bloodType = bloodType,
            profileImageUrl = imageBase64
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = dateOfBirth?.let { sdf.parse(it) } ?: Date()

        val cal = Calendar.getInstance()
        cal.time = birthDate
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val schedules = selectedVaccines.map { vaccine ->
            val doses = doseMap[vaccine.id] ?: emptyList()
            var currentDate = cal.time

            val doseSchedules = doses.mapIndexed { index, dose ->
                val intervalNumber = dose.intervalNumber ?: 0.0
                val intervalUnit = dose.intervalUnit ?: "Months"

                val intervalDays = when (intervalUnit.lowercase()) {
                    "days" -> intervalNumber
                    "weeks" -> intervalNumber * 7
                    "months" -> intervalNumber * 30.4375
                    "years" -> intervalNumber * 365
                    else -> 0.0
                }

                val doseCal = Calendar.getInstance()
                doseCal.time = currentDate
                doseCal.add(Calendar.DAY_OF_YEAR, intervalDays.toInt())
                currentDate = doseCal.time

                val doseDate = sdf.format(currentDate)

                BabyDoseSchedule(
                    doseName = dose.name,
                    interval = "$intervalNumber $intervalUnit",
                    date = doseDate,
                    isVisible = (index == 0),
                    isCompleted = false
                )
            }

            BabyVaccineSchedule(
                vaccineName = vaccine.name,
                vaccineType = vaccine.type,
                description = vaccine.description,
                route = vaccine.route,
                sideEffects = vaccine.sideEffects,
                lastGiven = null,
                doses = if (vaccine.hasDosage) doseSchedules else null
            )
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine<Unit> { cont ->
                        databaseService.addBabyWithSchedule(
                            userId, baby, schedules,
                            object : InterfaceClass.StatusCallback {
                                override fun onSuccess(message: String) {
                                    cont.resume(Unit) {}
                                }

                                override fun onError(error: String) {
                                    cont.resumeWith(Result.failure(Exception(error)))
                                }
                            })
                    }
                }

                binding.loadingOverlay.visibility = View.GONE
                DialogHelper.showSuccess(
                    this@select_vaccinePage,
                    "Successfully Saved",
                    "The baby info and schedules have been saved successfully!"
                ) { finish() }

            } catch (e: Exception) {
                binding.loadingOverlay.visibility = View.GONE
                Log.e("SaveBaby", "Error saving baby: ${e.message}")
            }
        }
    }


}
