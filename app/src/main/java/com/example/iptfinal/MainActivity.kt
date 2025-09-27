@file:Suppress("DEPRECATION")

package com.example.iptfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.components.bottomNav
import com.example.iptfinal.databinding.ActivityMainBinding
import com.example.iptfinal.pages.signup
import com.example.iptfinal.services.AuthServices

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private lateinit var authServices: AuthServices
    private lateinit var binding: ActivityMainBinding

    private val oneTapLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                authServices.handleSignInResult(data) { credential, error ->
                    if (credential != null) {
                        val profile = credential.profilePictureUri?.toString()
                        val username = credential.displayName?.toString()
                        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
                        sharedPref.edit {
                            putString("profile", profile)
                            putString("username",username)
                        }

                        Toast.makeText(
                            this,
                            "Successfully logged in with Google",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@MainActivity, bottomNav::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed: ${error ?: "Unknown error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        authServices = AuthServices(this)

        binding.googleLogin.setOnClickListener {
            authServices.signOut()
            authServices.signIn(this, oneTapLauncher)

        }
        binding.toSignupBtn.setOnClickListener {
            val intent: Intent = Intent(this@MainActivity, signup::class.java)
            startActivity(intent)
            finish()
        }
    }


}
