package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityAccountInfoPageBinding
import com.example.iptfinal.databinding.ActivityMainBinding

class AccountInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityAccountInfoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountInfo)) { v, insets ->
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
        if (!profile.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
        } else {
            val defaultProfile = R.drawable.profile
            Glide.with(this)
                .load(defaultProfile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)

        }

        Log.d(
            "AccountInfoLog",
            "Firstname: $firstname, Lastname: $lastname, Mobile: $mobile, Address: $address"
        )

        binding.firstnameTv.text = firstname
        binding.lastnameTv.text = lastname


        binding.emailTv.text = email

        if (address.isNullOrEmpty()) {
            binding.addressTv.text = "N/A"
        } else {
            binding.addressTv.text = address
        }
        if (mobile == "null") {
            binding.mobileTv.text = "N/A"
        } else {
            binding.mobileTv.text = mobile
        }



        binding.backButton.setOnClickListener {
            finish()
        }

    }


}