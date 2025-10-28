package com.example.iptfinal.pages

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.iptfinal.databinding.ActivityUpdateBabyInfoPageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Baby
import com.example.iptfinal.services.DatabaseService

class UpdateBabyInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBabyInfoPageBinding
    private val databaseService = DatabaseService()
    private lateinit var currentBaby: Baby
    private var isPopulatingFields = false 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateBabyInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val babyId = intent.getStringExtra("baby_id")
        if (babyId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Baby ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.formScrollView.visibility = View.GONE

        databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
            override fun onBabyLoaded(baby: Baby) {
                currentBaby = baby
                populateFields(baby)
                setupChangeListeners()
                binding.progressBar.visibility = View.GONE
                binding.formScrollView.visibility = View.VISIBLE
                toggleSaveButton()
            }

            override fun onError(error: String) {
                Toast.makeText(this@UpdateBabyInfoPage, error, Toast.LENGTH_SHORT).show()
            }
        })

        binding.backButton.setOnClickListener { onBackPressed() }

        binding.btnSaveBaby.setOnClickListener { updateBabyInfo() }
    }

    private fun populateFields(baby: Baby) {
        isPopulatingFields = true
        binding.etFullName.setText(baby.fullName)
        binding.etBirthPlace.setText(baby.birthPlace)
        binding.etWeight.setText(baby.weightAtBirth?.toString())
        binding.etHeight.setText(baby.heightAtBirth?.toString())
        binding.etBloodType.setText(baby.bloodType)
        binding.rbMale.isChecked = baby.gender == "Male"
        binding.rbFemale.isChecked = baby.gender == "Female"

        if (!baby.profileImageUrl.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(baby.profileImageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(this)
                    .load(bitmap)
                    .into(binding.profileImage)
                binding.profileImage.tag = baby.profileImageUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isPopulatingFields = false
    }

    private fun updateBabyInfo() {
        val updatedBaby = Baby(
            id = currentBaby.id,
            parentId = currentBaby.parentId,
            fullName = binding.etFullName.text.toString(),
            birthPlace = binding.etBirthPlace.text.toString(),
            gender = if (binding.rbMale.isChecked) "Male" else "Female",
            weightAtBirth = binding.etWeight.text.toString().toDoubleOrNull(),
            heightAtBirth = binding.etHeight.text.toString().toDoubleOrNull(),
            bloodType = binding.etBloodType.text.toString(),
            profileImageUrl = currentBaby.profileImageUrl
        )

        databaseService.updateBaby(
            updatedBaby.id ?: "",
            updatedBaby,
            object : InterfaceClass.StatusCallback {
                override fun onSuccess(message: String) {
                    Toast.makeText(this@UpdateBabyInfoPage, message, Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onError(error: String) {
                    Toast.makeText(this@UpdateBabyInfoPage, error, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupChangeListeners() {
        val textFields = listOf(
            binding.etFullName,
            binding.etBirthPlace,
            binding.etWeight,
            binding.etHeight,
            binding.etBloodType
        )
        textFields.forEach { editText ->
            editText.addTextChangedListener {
                if (!isPopulatingFields) toggleSaveButton()
            }
        }

        binding.rbMale.setOnCheckedChangeListener { _, _ -> if (!isPopulatingFields) toggleSaveButton() }
        binding.rbFemale.setOnCheckedChangeListener { _, _ -> if (!isPopulatingFields) toggleSaveButton() }

        binding.profileImage.setOnClickListener { if (!isPopulatingFields) toggleSaveButton() }
    }

    private fun toggleSaveButton() {
        val changed = hasChanges()
        binding.btnSaveBaby.isEnabled = changed
        binding.btnSaveBaby.alpha = if (changed) 1f else 0.5f
    }

    private fun hasChanges(): Boolean {
        return binding.etFullName.text.toString() != currentBaby.fullName ||
                binding.etBirthPlace.text.toString() != currentBaby.birthPlace ||
                binding.etWeight.text.toString() != currentBaby.weightAtBirth?.toString() ||
                binding.etHeight.text.toString() != currentBaby.heightAtBirth?.toString() ||
                binding.etBloodType.text.toString() != currentBaby.bloodType ||
                (binding.rbMale.isChecked && currentBaby.gender != "Male") ||
                (binding.rbFemale.isChecked && currentBaby.gender != "Female") ||
                binding.profileImage.tag != currentBaby.profileImageUrl
    }
}
