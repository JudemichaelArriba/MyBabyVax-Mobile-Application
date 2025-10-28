package com.example.iptfinal.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.databinding.ActivityUpdateBabyInfoPageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Baby
import com.example.iptfinal.services.DatabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UpdateBabyInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBabyInfoPageBinding
    private val databaseService = DatabaseService()
    private lateinit var currentBaby: Baby
    private var isPopulatingFields = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            binding.profileImage.setImageBitmap(bitmap)
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            binding.profileImage.tag = base64String
            toggleSaveButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateBabyInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val babyId = intent.getStringExtra("baby_id")
        if (babyId.isNullOrEmpty()) {
            DialogHelper.showError(this, "Error", "Baby ID missing") { finish() }
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.formScrollView.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                currentBaby = fetchBabyAsync(babyId)
                populateFields(currentBaby)
                setupChangeListeners()
                binding.progressBar.visibility = View.GONE
                binding.formScrollView.visibility = View.VISIBLE
                toggleSaveButton()
            } catch (e: Exception) {
                DialogHelper.showError(
                    this@UpdateBabyInfoPage,
                    "Error",
                    e.message ?: "Unknown error"
                )
            }
        }

        binding.backButton.setOnClickListener { onBackPressed() }

        binding.btnSaveBaby.setOnClickListener {
            DialogHelper.showWarning(
                this,
                "Confirm",
                "Do you want to save changes?",
                onConfirm = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        saveBabyInfo()
                    }
                }
            )
        }

        binding.btnUploadImage.setOnClickListener {
            if (!isPopulatingFields) pickImageLauncher.launch(
                "image/*"
            )
        }
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

    private fun setupChangeListeners() {
        val textFields = listOf(
            binding.etFullName,
            binding.etBirthPlace,
            binding.etWeight,
            binding.etHeight,
            binding.etBloodType
        )
        textFields.forEach { editText -> editText.addTextChangedListener { if (!isPopulatingFields) toggleSaveButton() } }
        binding.rbMale.setOnCheckedChangeListener { _, _ -> if (!isPopulatingFields) toggleSaveButton() }
        binding.rbFemale.setOnCheckedChangeListener { _, _ -> if (!isPopulatingFields) toggleSaveButton() }
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

    private suspend fun fetchBabyAsync(babyId: String): Baby = suspendCancellableCoroutine { cont ->
        databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
            override fun onBabyLoaded(baby: Baby) {
                cont.resume(baby)
            }

            override fun onError(error: String) {
                cont.resumeWithException(Exception(error))
            }
        })
    }

    private suspend fun saveBabyInfo() {
        val updatedBaby = Baby(
            id = currentBaby.id,
            parentId = currentBaby.parentId,
            fullName = binding.etFullName.text.toString(),
            birthPlace = binding.etBirthPlace.text.toString(),
            gender = if (binding.rbMale.isChecked) "Male" else "Female",
            weightAtBirth = binding.etWeight.text.toString().toDoubleOrNull(),
            heightAtBirth = binding.etHeight.text.toString().toDoubleOrNull(),
            bloodType = binding.etBloodType.text.toString(),
            profileImageUrl = binding.profileImage.tag as? String ?: currentBaby.profileImageUrl
        )

        try {
            suspendCancellableCoroutine<Unit> { cont ->
                databaseService.updateBaby(
                    updatedBaby.id ?: "",
                    updatedBaby,
                    object : InterfaceClass.StatusCallback {
                        override fun onSuccess(message: String) {
                            cont.resume(Unit)
                        }

                        override fun onError(error: String) {
                            cont.resumeWithException(Exception(error))
                        }
                    })
            }
            DialogHelper.showSuccess(this, "Success", "Baby info updated successfully") { finish() }
        } catch (e: Exception) {
            DialogHelper.showError(this, "Error", "Failed to update baby: ${e.message}")
        }
    }
}
