package com.example.iptfinal.pages

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.adapters.VaccineAdapter
import com.example.iptfinal.databinding.ActivitySelectVaccinePageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Dose
import com.example.iptfinal.models.Vaccine
import com.example.iptfinal.services.DatabaseService
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class select_vaccinePage : AppCompatActivity() {

    private lateinit var binding: ActivitySelectVaccinePageBinding
    private val databaseService = DatabaseService()

    private var fullName: String? = null
    private var gender: String? = null
    private var dateOfBirth: String? = null
    private var birthPlace: String? = null
    private var weight: String? = null
    private var height: String? = null
    private var bloodType: String? = null
    private var babyImageUriString: String? = null
    private var babyImageUri: Uri? = null

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
                    binding.recyclerViewVaccines.adapter =
                        VaccineAdapter(this@select_vaccinePage, filteredVaccines, dosesMap)


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

    fun getBabyImageUri(): Uri? = babyImageUri
}
