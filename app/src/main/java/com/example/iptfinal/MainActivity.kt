@file:Suppress("DEPRECATION")

package com.example.iptfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.databinding.ActivityMainBinding
import com.example.iptfinal.pages.signup
import com.example.iptfinal.services.AuthServices

import com.google.android.gms.auth.api.signin.GoogleSignIn


class MainActivity : AppCompatActivity() {

    private lateinit var authServices: AuthServices
    private lateinit var binding: ActivityMainBinding


    private val oneTapLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                authServices.handleSignInResult(data) { success, error ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "Successfully logged in with Google",
                            Toast.LENGTH_SHORT
                        ).show()
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
