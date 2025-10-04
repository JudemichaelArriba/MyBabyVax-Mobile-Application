package com.example.iptfinal.pages

import android.content.Intent
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
import com.example.iptfinal.components.bottomNav
import com.example.iptfinal.databinding.ActivityAccountInfoPageBinding
import com.example.iptfinal.services.SessionManager


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


        val sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user != null) {

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


            binding.firstnameTv.text = user.firstname.ifEmpty { "N/A" }
            binding.lastnameTv.text = user.lastname.ifEmpty { "N/A" }
            binding.emailTv.text = user.email.ifEmpty { "N/A" }
            binding.addressTv.text = user.address.ifEmpty { "N/A" }
            binding.mobileTv.text =
                if (user.mobileNum.isEmpty() || user.mobileNum == "null") "N/A" else user.mobileNum

            Log.d(
                "AccountInfoLog",
                "Firstname: ${user.firstname}, Lastname: ${user.lastname}, " +
                        "Mobile: ${user.mobileNum}, Address: ${user.address}"
            )
        } else {

            binding.firstnameTv.text = "N/A"
            binding.lastnameTv.text = "N/A"
            binding.emailTv.text = "N/A"
            binding.addressTv.text = "N/A"
            binding.mobileTv.text = "N/A"

            Glide.with(this)
                .load(R.drawable.profile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
        }

        binding.editBtn.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
            finish()
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
