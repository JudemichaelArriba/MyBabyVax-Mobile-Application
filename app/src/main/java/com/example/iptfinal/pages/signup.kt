package com.example.iptfinal.pages

import android.content.Intent
import android.os.Bundle
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
import com.example.iptfinal.services.DatabaseService

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        dbService.fetchPuroks(object : DatabaseService.PurokCallback {
            override fun onPuroksLoaded(puroks: List<String>) {
                val adapter = ArrayAdapter(
                    this@signup,
                    android.R.layout.simple_spinner_item,
                    puroks
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.purokDropdown.setAdapter(adapter)
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



        binding.signupBtn.setOnClickListener {


        }

        binding.toLoginBtn.setOnClickListener {

            val intent: Intent = Intent(this@signup, MainActivity::class.java)
            startActivity(intent)
            finish()


        }
    }
}