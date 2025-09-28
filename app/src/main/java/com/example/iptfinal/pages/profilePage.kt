package com.example.iptfinal.pages

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.databinding.FragmentProfilePageBinding


class profilePage : Fragment() {


    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.white)
        val sharedPref = requireActivity()
            .getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val profile = sharedPref.getString("profile", null)
        val username = sharedPref.getString("username", null)
        val email = sharedPref.getString("email", null)

        binding.emailText.text = email

        binding.username.text = username

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
    }


}
