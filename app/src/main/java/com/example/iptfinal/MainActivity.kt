@file:Suppress("DEPRECATION")

package com.example.iptfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.components.Dialogs
import com.example.iptfinal.components.bottomNav
import com.example.iptfinal.databinding.ActivityMainBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Users
import com.example.iptfinal.pages.signup
import com.example.iptfinal.services.AuthServices
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var authServices: AuthServices
    private lateinit var binding: ActivityMainBinding
    private val myDialog = Dialogs(this)
private  val DatabaseService = DatabaseService()

    private lateinit var sessionManager: SessionManager

    private val oneTapLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                authServices.handleSignInResult(data) { credential, error ->
                    if (credential != null) {

                        binding.loadingOverlay.visibility = View.VISIBLE

                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        val profile = credential.profilePictureUri?.toString()
                        val username = credential.displayName?.toString()
                        val firstname = credential.givenName.toString()
                        val lastname = credential.familyName.toString()
                        val mobileNum = credential.phoneNumber.toString()

                        if (firebaseUser != null) {
                            val userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(firebaseUser.uid)


                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {

                                        sessionManager.saveUser(snapshot.getValue(Users::class.java)!!)
                                        sessionManager.setGoogleLogin(true)
                                        DatabaseService.saveFcmToken()
                                        binding.loadingOverlay.visibility = View.GONE
                                        DialogHelper.showSuccess(
                                            this@MainActivity,
                                            "Login Successful",
                                            "Welcome back ${username}!"
                                        ) {
                                            val intent = Intent(this@MainActivity, bottomNav::class.java)
                                            startActivity(intent)
                                            finish()
                                        }

                                    } else {

                                        val newUser = Users(
                                            uid = firebaseUser.uid,
                                            firstname = firstname,
                                            lastname = lastname,
                                            email = firebaseUser.email ?: "",
                                            address = "",
                                            mobileNum = mobileNum,
                                            profilePic = profile.toString(),
                                            role = "User"
                                        )

                                        userRef.setValue(newUser)
                                            .addOnSuccessListener {
                                                sessionManager.saveUser(newUser)
                                                sessionManager.setGoogleLogin(true)
                                                binding.loadingOverlay.visibility = View.GONE

                                                DialogHelper.showSuccess(
                                                    this@MainActivity,
                                                    "Login Successful",
                                                    "Welcome ${username}!"
                                                ) {
                                                    val intent =
                                                        Intent(this@MainActivity, bottomNav::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                binding.loadingOverlay.visibility = View.GONE
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Failed to create user: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    binding.loadingOverlay.visibility = View.GONE
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Database error: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        } else {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(this, "Firebase user is null", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(
                            this,
                            "Failed: ${error ?: "Unknown error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.loadingOverlay.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
                binding.loadingOverlay.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sessionManager = SessionManager(this)


        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this@MainActivity, bottomNav::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.loginBtn.setOnClickListener {
            binding.loadingOverlay.visibility = View.VISIBLE
            val email = binding.emailTv.text.toString().trim()
            val password = binding.passwordTv.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.loadingOverlay.visibility = View.GONE
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            authServices.signInWithEmail(email, password) { user, error ->
                if (user != null) {
                    val databaseService = DatabaseService()
                    databaseService.fetchUserById(user.uid, object : InterfaceClass.UserCallback {

                        override fun onUserLoaded(userData: Users) {


                            if (userData.role == "User") {
                                sessionManager.saveUser(userData)
                                sessionManager.setGoogleLogin(false)

                                Toast.makeText(
                                    this@MainActivity,
                                    "Welcome ${userData.firstname}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                binding.loadingOverlay.visibility = View.GONE
                                val intent = Intent(this@MainActivity, bottomNav::class.java)
                                startActivity(intent)
                                finish()

                            } else {

                                FirebaseAuth.getInstance().signOut()
                                binding.loadingOverlay.visibility = View.GONE
                                DialogHelper.showError(
                                    this@MainActivity,
                                    "Access Denied",
                                    "Only user accounts can log in through this app."
                                )
                            }
                        }

                        override fun onError(message: String) {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, error ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        authServices = AuthServices(this)


        binding.googleLogin.setOnClickListener {
            binding.loadingOverlay.visibility = View.VISIBLE
            authServices.signOut()
            authServices.signIn(this, oneTapLauncher)
        }


        binding.toSignupBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, signup::class.java)
            startActivity(intent)
            finish()
        }
    }
}
