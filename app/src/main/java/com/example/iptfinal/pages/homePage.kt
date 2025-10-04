package com.example.iptfinal.pages

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.databinding.FragmentHomePageBinding
import com.example.iptfinal.services.SessionManager
import com.example.iptfinal.models.Users

class homePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.white)


        val sessionManager = SessionManager(requireContext())
        val user: Users = sessionManager.getUser()


        val username = if (user.firstname.isNotEmpty() || user.lastname.isNotEmpty()) {
            "${user.firstname} ${user.lastname}"
        } else {
            "Username"
        }
        binding.username.text = username


        if (user.profilePic.isNotEmpty()) {
            Glide.with(this)
                .load(user.profilePic)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
        } else {
            Glide.with(this)
                .load(R.drawable.profile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
        }


        binding.history.setOnClickListener {
            val intent = Intent(requireContext(), HistoryPage::class.java)
            startActivity(intent)
        }

        binding.schedule.setOnClickListener {
            val intent = Intent(requireContext(), SchedulePage::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
