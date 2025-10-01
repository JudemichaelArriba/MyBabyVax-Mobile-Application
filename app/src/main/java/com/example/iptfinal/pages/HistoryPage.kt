package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityAccountInfoPageBinding

import com.example.iptfinal.databinding.ActivityHistoryPageBinding
import kotlin.math.log

class HistoryPage : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.historyPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val profile = sharedPref.getString("profile", "N/A")
        val username = sharedPref.getString("username", "N/A")
        val email = sharedPref.getString("email", "N/A")
        val uid = sharedPref.getString("uid", "N/A")
        val address = sharedPref.getString("address", "N/A")
        val mobile = sharedPref.getString("mobileNum", "N/A")
        val firstname = sharedPref.getString("firstname", "N/A")
        val lastname = sharedPref.getString("lastname", "N/A")

        Log.d(
            "HistoryPageLog", "Username $username"
        )

        binding.usernameTv.text = username
        binding.backButton.setOnClickListener {
            finish()
        }

    }
}