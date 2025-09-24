package com.example.iptfinal.components

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityBottomNavBinding
import com.example.iptfinal.databinding.ActivityMainBinding

class bottomNav : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.bottomNav.selectedItemId = R.id.home


        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.home ->{
                    true
                }
                R.id.baby ->{
                    true
                }
                R.id.notification ->{
                    true
                }
                R.id.profile ->{
                    true
                }else -> false
            }
        }



    }





}