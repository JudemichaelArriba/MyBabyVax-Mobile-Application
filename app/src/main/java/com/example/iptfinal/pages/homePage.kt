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
import com.example.iptfinal.databinding.FragmentHomePageBinding
import kotlin.math.log


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
        val sharedPref = requireActivity()
            .getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val profile = sharedPref.getString("profile", null)
        var username = sharedPref.getString("username", null)



        if (!username.isNullOrEmpty()) {
            binding.username.text = username
        } else {
            username = "Username"
            binding.username.text = username
        }

        if (profile.isNullOrEmpty()) {
            val defaultProfile = R.drawable.profile
            Glide.with(this)
                .load(defaultProfile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)

        }
        if (!profile.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.default_profile)
                .into(binding.profileImage)
        }
    }

}