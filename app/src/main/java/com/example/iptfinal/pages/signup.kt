package com.example.iptfinal.pages

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.hbb20.CountryCodePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.MainActivity
import com.example.iptfinal.R
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.databinding.ActivitySignupBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Users
import com.example.iptfinal.services.AuthServices
import com.example.iptfinal.services.DatabaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val dbService = DatabaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom + ime.bottom
            )
            insets
        }


        val ccp: CountryCodePicker = binding.countryCodePicker
        ccp.setCountryForNameCode("PH")
        ccp.setAutoDetectedCountry(true)
        ccp.registerCarrierNumberEditText(binding.mobileNumber)

        binding.mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var input = s.toString()
                if (input.startsWith("0")) {
                    input = input.drop(1)
                    binding.mobileNumber.setText(input)
                    binding.mobileNumber.setSelection(input.length)
                }
                if (input.length > 12) {
                    input = input.substring(0, 12)
                    binding.mobileNumber.setText(input)
                    binding.mobileNumber.setSelection(12)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        dbService.fetchPuroks(object : InterfaceClass.PurokCallback {
            override fun onPuroksLoaded(puroks: List<String>) {
                val adapter = android.widget.ArrayAdapter(
                    this@signup,
                    android.R.layout.simple_spinner_item,
                    puroks
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.purokDropdown.setAdapter(adapter)
                binding.purokDropdown.setTextColor(getColor(R.color.mainColor))
            }

            override fun onError(message: String) {
                DialogHelper.showError(this@signup, "Error", "Failed to load puroks: $message")
            }
        })


        binding.emailTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (!isValidEmail(email)) {
                    binding.emailLayout.boxStrokeColor =
                        getColor(com.google.android.material.R.color.design_default_color_error)
                    binding.emailLayout.helperText = "Invalid email"
                    binding.emailLayout.setHelperTextColor(
                        ColorStateList.valueOf(
                            getColor(com.google.android.material.R.color.design_default_color_error)
                        )
                    )
                } else {
                    binding.emailLayout.boxStrokeColor = getColor(R.color.mainColor)
                    binding.emailLayout.helperText = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val confirmPass = s.toString()
                if (confirmPass != binding.passwordEditText.text.toString().trim()) {
                    binding.ConfirmPasswordInputLayout.boxStrokeColor =
                        getColor(com.google.android.material.R.color.design_default_color_error)
                    binding.ConfirmPasswordInputLayout.helperText = "Password did not match"
                    binding.ConfirmPasswordInputLayout.setHelperTextColor(
                        ColorStateList.valueOf(
                            getColor(com.google.android.material.R.color.design_default_color_error)
                        )
                    )
                } else {
                    binding.ConfirmPasswordInputLayout.boxStrokeColor = getColor(R.color.mainColor)
                    binding.ConfirmPasswordInputLayout.helperText = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.signupBtn.setOnClickListener {
            val firstname = binding.firstname.text.toString().trim()
            val lastname = binding.lastname.text.toString().trim()
            val email = binding.emailTxt.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val address = binding.purokDropdown.text.toString().trim()
            val confirmPass = binding.confirmPasswordEditText.text.toString().trim()
            val fullMobileNum = "+" + ccp.fullNumber.trim()

            if (email.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty()) {
                DialogHelper.showWarning(this, "Warning", "Please fill all required fields")
                return@setOnClickListener
            }

            if (confirmPass != password) {
                DialogHelper.showWarning(
                    this,
                    "Warning",
                    "Password and confirm password did not match"
                )
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().getReference("users")

            database.get().addOnSuccessListener { snapshot ->
                var sameFirstName = false
                var sameLastName = false
                var sameMobile = false

                for (userSnap in snapshot.children) {
                    val dbFirstname = userSnap.child("firstname").value?.toString()?.trim() ?: ""
                    val dbLastname = userSnap.child("lastname").value?.toString()?.trim() ?: ""
                    val dbMobile = userSnap.child("mobileNum").value?.toString()?.trim() ?: ""

                    if (dbFirstname.equals(firstname, ignoreCase = true)) sameFirstName = true
                    if (dbLastname.equals(lastname, ignoreCase = true)) sameLastName = true
                    if (dbMobile == fullMobileNum) sameMobile = true
                }

                when {
                    sameFirstName && sameLastName && sameMobile -> {
                        DialogHelper.showWarning(
                            this,
                            "Warning",
                            "A user with the same name and contact number already exists"
                        )
                    }

                    sameFirstName && sameLastName -> {
                        DialogHelper.showWarning(
                            this,
                            "Warning",
                            "A user with the same name already exists"
                        )
                    }

                    sameMobile -> {
                        DialogHelper.showWarning(
                            this,
                            "Warning",
                            "This contact number is already registered"
                        )
                    }

                    else -> {
                        val authServices = AuthServices(this@signup)
                        authServices.signUpWithEmail(email, password) { user, error ->
                            if (user != null) {
                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                if (firebaseUser != null) {
                                    val newUser = Users(
                                        uid = firebaseUser.uid,
                                        firstname = firstname,
                                        lastname = lastname,
                                        email = firebaseUser.email ?: "",
                                        address = address,
                                        mobileNum = fullMobileNum,
                                        profilePic = "",
                                        role = "User"
                                    )
                                    database.child(firebaseUser.uid).setValue(newUser)
                                }
                                DialogHelper.showSuccess(this, "Success", "Sign up successful!") {
                                    startActivity(Intent(this@signup, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                DialogHelper.showError(this, "Error", error ?: "Unknown error")
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                DialogHelper.showError(this, "Error", "Error checking existing users")
            }
        }

        binding.toLoginBtn.setOnClickListener {
            startActivity(Intent(this@signup, MainActivity::class.java))
            finish()
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
