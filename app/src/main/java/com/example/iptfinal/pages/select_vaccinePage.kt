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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

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

        binding.btnSave.isEnabled = false
        binding.btnSave.alpha = 0.5f

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
                            "days" -> doseCal.add(Calendar.DAY_OF_YEAR, (dose.intervalNumber ?: 0.0).toInt())
                            "weeks" -> doseCal.add(Calendar.DAY_OF_YEAR, ((dose.intervalNumber ?: 0.0) * 7).toInt())
                            "months" -> {
                                val months = (dose.intervalNumber ?: 0.0).toInt()
                                val halfMonth = if ((dose.intervalNumber ?: 0.0) % 1 >= 0.5) 15 else 0
                                doseCal.add(Calendar.MONTH, months)
                                doseCal.add(Calendar.DAY_OF_YEAR, halfMonth)
                            }
                            "years" -> doseCal.add(Calendar.YEAR, (dose.intervalNumber ?: 0.0).toInt())
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

                vaccineAdapter = VaccineAdapter(
                    this@select_vaccinePage,
                    vaccines,
                    babyDoseMap
                )

                binding.recyclerViewVaccines.apply {
                    layoutManager = LinearLayoutManager(this@select_vaccinePage)
                    adapter = vaccineAdapter
                    visibility = View.VISIBLE
                }

                binding.progressBarLoading.visibility = View.GONE
                binding.btnSave.isEnabled = true
                binding.btnSave.alpha = 1f

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

        val userBabiesRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("babies")

        userBabiesRef.get().addOnSuccessListener { snapshot ->
            var duplicate = false
            for (babySnap in snapshot.children) {
                val existingName = babySnap.child("fullName").value?.toString()?.trim() ?: ""
                if (existingName.equals(fullName, ignoreCase = true)) {
                    duplicate = true
                    break
                }
            }

            if (duplicate) {
                binding.loadingOverlay.visibility = View.GONE
                DialogHelper.showError(
                    this@select_vaccinePage,
                    "Duplicate Baby",
                    "A baby with the same name already exists."
                )
                return@addOnSuccessListener
            }

            val updatedDoseMap = vaccineAdapter.getUpdatedVaccineSchedules()
            saveBabyAndScheduleToDatabase(userId, baby, updatedDoseMap)
        }.addOnFailureListener {
            binding.loadingOverlay.visibility = View.GONE
            DialogHelper.showError(
                this@select_vaccinePage,
                "Error",
                "Failed to check existing babies."
            )
        }
    }

    private fun saveBabyAndScheduleToDatabase(
        userId: String,
        baby: Baby,
        updatedDoseMap: Map<String, MutableList<BabyDoseSchedule>>
    ) {
        lifecycleScope.launch {
            try {
                val schedules = selectedVaccines.map { vaccine ->
                    val doseSchedules = updatedDoseMap[vaccine.id] ?: mutableListOf()
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