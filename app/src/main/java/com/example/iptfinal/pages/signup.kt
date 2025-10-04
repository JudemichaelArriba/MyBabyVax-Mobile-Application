package com.example.iptfinal.pages

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.MainActivity
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityMainBinding
import com.example.iptfinal.databinding.ActivitySignupBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Users
import com.example.iptfinal.services.AuthServices
import com.example.iptfinal.services.DatabaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class signup : AppCompatActivity() {


    private lateinit var binding: ActivitySignupBinding
    val dbService = DatabaseService()


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


        dbService.fetchPuroks(object : InterfaceClass.PurokCallback {
            override fun onPuroksLoaded(puroks: List<String>) {
                val adapter = ArrayAdapter(
                    this@signup,
                    android.R.layout.simple_spinner_item,
                    puroks
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.purokDropdown.setAdapter(adapter)
                binding.purokDropdown.setTextColor(getColor(R.color.mainColor))
                if (!adapter.isEmpty) {
                    for (i in 0 until adapter.count) {
                        val item = adapter.getItem(i)
                        Log.d("SignupLog", "Item $i: $item")
                    }
                } else {
                    Log.d("SignupLog", "Adapter is empty")
                }
            }

            override fun onError(message: String) {
                Toast.makeText(this@signup, "Failed to load puroks: $message", Toast.LENGTH_SHORT)
                    .show()
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
                        ColorStateList.valueOf(getColor(com.google.android.material.R.color.design_default_color_error))
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
                        ColorStateList.valueOf(getColor(com.google.android.material.R.color.design_default_color_error))
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
            val mobileNum = binding.mobileNumber.text.toString().trim()
            val address = binding.purokDropdown.text.toString().trim()
            val confirmPass = binding.confirmPasswordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty()) {
                Toast.makeText(this@signup, "Please fill all required fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (confirmPass != password) {
                Toast.makeText(
                    this@signup,
                    "Password and confirm password did not match",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val authServices = AuthServices(this@signup)


            authServices.signUpWithEmail(email, password) { user, error ->
                if (user != null) {

                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val user = Users(
                            uid = firebaseUser.uid,
                            firstname = firstname,
                            lastname = lastname,
                            email = firebaseUser.email ?: "",
                            address = address,
                            mobileNum = mobileNum,
                            profilePic = "",
                            role = "User"

                        )
                        val database = FirebaseDatabase.getInstance()
                        database.getReference("users")
                            .child(firebaseUser.uid)
                            .setValue(user)
                    }


                    Toast.makeText(this@signup, "Sign up successful!", Toast.LENGTH_SHORT).show()


                } else {
                    Toast.makeText(this@signup, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.toLoginBtn.setOnClickListener {

            val intent: Intent = Intent(this@signup, MainActivity::class.java)
            startActivity(intent)
            finish()


        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}