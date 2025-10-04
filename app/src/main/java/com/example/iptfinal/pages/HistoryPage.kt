package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.R
import com.example.iptfinal.databinding.ActivityHistoryPageBinding
import com.example.iptfinal.services.SessionManager

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


        val sessionManager = SessionManager(this)
        val user = sessionManager.getUser()


        val username = "${user.firstname} ${user.lastname}"
        Log.d("HistoryPageLog", "Username: $username")

        binding.usernameTv.text = username

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
