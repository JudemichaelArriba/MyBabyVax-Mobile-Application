package com.example.iptfinal.pages

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.MainActivity
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityMainBinding
import com.example.iptfinal.databinding.ActivitySignupBinding

class signup : AppCompatActivity() {


    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toLoginBtn.setOnClickListener {

            val intent: Intent = Intent(this@signup, MainActivity::class.java)
            startActivity(intent)
            finish()


        }
    }
}