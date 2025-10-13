package com.example.iptfinal.pages

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.databinding.ActivityAddBabyPageBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddBabyPage : AppCompatActivity() {

    private lateinit var binding: ActivityAddBabyPageBinding
    private val calendar = Calendar.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.profileImage)
        }
        checkAllFields()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBabyPageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.AddBabyPage) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bloodTypes)
        binding.etBloodType.setAdapter(adapter)
        binding.btnSaveBaby.isEnabled = false
        binding.btnSaveBaby.alpha = 0.5f

        binding.etBloodType.setOnClickListener {
            binding.etBloodType.showDropDown()
        }
        binding.backButton.setOnClickListener { finish() }
        binding.etDateOfBirth.setOnClickListener { showDatePicker() }
        binding.btnUploadImage.setOnClickListener { pickImageFromGallery() }
        binding.btnSaveBaby.setOnClickListener { passBabyDataToNextPage() }

        addTextWatchers()
        binding.rgGender.setOnCheckedChangeListener { _, _ ->
            checkAllFields()
        }
    }

    private fun addTextWatchers() {
        val textInputs = listOf(
            binding.etFullName,
            binding.etDateOfBirth,
            binding.etBirthPlace,
            binding.etWeight,
            binding.etHeight,
            binding.etBloodType
        )

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAllFields()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        textInputs.forEach { it.addTextChangedListener(watcher) }
    }

    private fun checkAllFields() {
        val fullName = binding.etFullName.text.toString().trim()
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val birthPlace = binding.etBirthPlace.text.toString().trim()
        val weight = binding.etWeight.text.toString().trim()
        val height = binding.etHeight.text.toString().trim()
        val bloodType = binding.etBloodType.text.toString().trim()
        val genderSelected = binding.rbMale.isChecked || binding.rbFemale.isChecked
        val hasImage = selectedImageUri != null

        val allFilled = fullName.isNotEmpty() &&
                dateOfBirth.isNotEmpty() &&
                birthPlace.isNotEmpty() &&
                weight.isNotEmpty() &&
                height.isNotEmpty() &&
                bloodType.isNotEmpty() &&
                genderSelected &&
                hasImage

        binding.btnSaveBaby.isEnabled = allFilled
        binding.btnSaveBaby.alpha = if (allFilled) 1f else 0.5f
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.etDateOfBirth.setText(dateFormat.format(selectedDate.time))
                checkAllFields()
            },
            year, month, day
        )
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun passBabyDataToNextPage() {
        val fullName = binding.etFullName.text.toString().trim()
        val gender = when {
            binding.rbMale.isChecked -> "Male"
            binding.rbFemale.isChecked -> "Female"
            else -> ""
        }
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val birthPlace = binding.etBirthPlace.text.toString().trim()
        val weight = binding.etWeight.text.toString().trim()
        val height = binding.etHeight.text.toString().trim()
        val bloodType = binding.etBloodType.text.toString().trim()

        val intent = Intent(this, select_vaccinePage::class.java)
        intent.putExtra("fullName", fullName)
        intent.putExtra("gender", gender)
        intent.putExtra("dateOfBirth", dateOfBirth)
        intent.putExtra("birthPlace", birthPlace)
        intent.putExtra("weight", weight)
        intent.putExtra("height", height)
        intent.putExtra("bloodType", bloodType)
        intent.putExtra("imageUri", selectedImageUri?.toString())
        startActivity(intent)
    }
}
