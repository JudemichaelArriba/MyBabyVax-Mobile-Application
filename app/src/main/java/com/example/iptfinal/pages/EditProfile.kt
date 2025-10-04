package com.example.iptfinal.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.databinding.ActivityEditProfileBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import androidx.activity.result.contract.ActivityResultContracts
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val dbService = DatabaseService()
    private var selectedImageUri: Uri? = null


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.default_profile)
                    .into(binding.profileImage)
                setEditButtonEnabled(true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        WindowCompat.setDecorFitsSystemWindows(window, false)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())


            val bottomPadding = maxOf(systemBarsInsets.bottom, imeInsets.bottom)

            view.setPadding(
                0,
                systemBarsInsets.top,
                0,
                bottomPadding
            )

            insets
        }
        val sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user.profilePic.isNotEmpty()) {
            try {

                if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                    val imageBytes =
                        android.util.Base64.decode(user.profilePic, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.size
                    )
                    binding.profileImage.setImageBitmap(bitmap)
                } else {

                    Glide.with(this)
                        .load(user.profilePic)
                        .placeholder(R.drawable.default_profile)
                        .into(binding.profileImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.profileImage.setImageResource(R.drawable.default_profile)
            }
        } else {
            binding.profileImage.setImageResource(R.drawable.default_profile)
        }

        binding.firstnameTv.setText(user.firstname)
        binding.lastnameTv.setText(user.lastname)
        binding.emailTv.setText(user.email)
        binding.addressTv.setText(if (user.address.isNullOrEmpty()) "N/A" else user.address)
        binding.mobileTv.setText(if (user.mobileNum == "null") "N/A" else user.mobileNum)

        val isGoogleAccount = sessionManager.isGoogleLogin()

        if (isGoogleAccount) {
            binding.firstnameTv.isEnabled = false
            binding.lastnameTv.isEnabled = false
            binding.emailTv.isEnabled = false

            binding.addressTv.isEnabled = true
            binding.mobileTv.isEnabled = true

            binding.firstnameTv.alpha = 0.7f
            binding.lastnameTv.alpha = 0.7f
            binding.emailTv.alpha = 0.7f

            binding.firstnameTv.hint = "Google account (read-only)"
            binding.lastnameTv.hint = "Google account (read-only)"
            binding.emailTv.hint = "Google account (read-only)"

            binding.uploadBtn.isEnabled = false
            binding.uploadBtn.alpha = 0.5f
            binding.uploadBtn.isClickable = false
        }

        binding.editBtn.isEnabled = false
        binding.editBtn.alpha = 0.5f


        binding.uploadBtn.setOnClickListener {
            openGallery()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val updatedUser = user.copy(
                    firstname = binding.firstnameTv.text.toString().trim(),
                    lastname = binding.lastnameTv.text.toString().trim(),
                    email = binding.emailTv.text.toString().trim(),
                    address = binding.addressTv.text.toString().trim(),
                    mobileNum = binding.mobileTv.text.toString().trim()
                )

                val hasChanges = updatedUser != user

                if (isGoogleAccount) {
                    val addressChanged = binding.addressTv.text.toString().trim() != user.address
                    val mobileChanged = binding.mobileTv.text.toString().trim() != user.mobileNum
                    setEditButtonEnabled(addressChanged || mobileChanged)
                } else {
                    setEditButtonEnabled(hasChanges)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.firstnameTv.addTextChangedListener(watcher)
        binding.lastnameTv.addTextChangedListener(watcher)
        binding.emailTv.addTextChangedListener(watcher)
        binding.addressTv.addTextChangedListener(watcher)
        binding.mobileTv.addTextChangedListener(watcher)

        binding.backButton.setOnClickListener {
            finish()
        }


        binding.editBtn.setOnClickListener {
            val updatedUser = user.copy(
                firstname = binding.firstnameTv.text.toString().trim(),
                lastname = binding.lastnameTv.text.toString().trim(),
                email = binding.emailTv.text.toString().trim(),
                address = binding.addressTv.text.toString().trim(),
                mobileNum = binding.mobileTv.text.toString().trim()
            )

            if (updatedUser == user && selectedImageUri == null) {
                DialogHelper.showWarning(
                    this,
                    "No Changes Detected",
                    "You haven’t modified any profile information."
                )
                return@setOnClickListener
            }

            DialogHelper.showWarning(
                this,
                "Update Profile",
                "Are you sure you want to save these changes?",
                onConfirm = {

                    if (selectedImageUri != null) {
                        val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        val baos = java.io.ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos)
                        val imageBytes = baos.toByteArray()
                        val base64Image = android.util.Base64.encodeToString(
                            imageBytes,
                            android.util.Base64.DEFAULT
                        )

                        val updatedUserWithImage = updatedUser.copy(profilePic = base64Image)

                        dbService.updateUser(
                            user.uid,
                            updatedUserWithImage,
                            object : InterfaceClass.StatusCallback {
                                override fun onSuccess(message: String) {
                                    sessionManager.saveUser(updatedUserWithImage)
                                    DialogHelper.showSuccess(
                                        this@EditProfile,
                                        "Success",
                                        message
                                    ) {
                                        finish()
                                    }
                                }

                                override fun onError(message: String) {
                                    DialogHelper.showError(
                                        this@EditProfile,
                                        "Error",
                                        message
                                    )
                                }
                            })
                    } else {

                        dbService.updateUser(
                            user.uid,
                            updatedUser,
                            object : InterfaceClass.StatusCallback {
                                override fun onSuccess(message: String) {
                                    sessionManager.saveUser(updatedUser)
                                    DialogHelper.showSuccess(
                                        this@EditProfile,
                                        "Success",
                                        message
                                    ) {
                                        finish()
                                    }
                                }

                                override fun onError(message: String) {
                                    DialogHelper.showError(
                                        this@EditProfile,
                                        "Error",
                                        message
                                    )
                                }
                            })
                    }
                }
            )
        }




        dbService.fetchPuroks(object : InterfaceClass.PurokCallback {
            override fun onPuroksLoaded(puroks: List<String>) {
                val adapter = ArrayAdapter(
                    this@EditProfile,
                    android.R.layout.simple_spinner_item,
                    puroks
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.addressTv.setAdapter(adapter)
                binding.addressTv.setTextColor(getColor(R.color.mainColor))
                if (!adapter.isEmpty) {
                    for (i in 0 until adapter.count) {
                        val item = adapter.getItem(i)
                        Log.d("EditProfile", "Item $i: $item")
                    }
                } else {
                    Log.d("EditProfile", "Adapter is empty")
                }
            }

            override fun onError(message: String) {
                Toast.makeText(
                    this@EditProfile,
                    "Failed to load puroks: $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setEditButtonEnabled(enabled: Boolean) {
        binding.editBtn.isEnabled = enabled
        binding.editBtn.alpha = if (enabled) 1f else 0.5f
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
}
