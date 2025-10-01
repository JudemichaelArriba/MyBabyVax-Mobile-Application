package com.example.iptfinal.pages

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.MainActivity
import com.example.iptfinal.R
import com.example.iptfinal.databinding.FragmentProfilePageBinding
import com.example.iptfinal.services.AuthServices
import androidx.core.content.edit
import com.example.iptfinal.components.bottomNav


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
        val uid = sharedPref.getString("uid", null)
        val address = sharedPref.getString("address", null)
        val mobile = sharedPref.getString("mobileNum", null)
        val firstname = sharedPref.getString("firstname", null)
        val lastname = sharedPref.getString("lastname", null)


        binding.emailText.text = email

        Log.d(
            "ProfilePage",
            "Firstname: $firstname, Lastname: $lastname, Mobile: $mobile, Address: $address"
        )

        binding.accountInfo.setOnClickListener {
            val intent = Intent(requireContext(), AccountInfoPage::class.java)
            startActivity(intent)

        }
        binding.username.text = username

        binding.card1.setOnClickListener {
            val intent = Intent(requireContext(), AccountInfoPage::class.java)
            startActivity(intent)

        }










        binding.logout.setOnClickListener {

            binding.logout.setOnClickListener {
                com.example.iptfinal.components.DialogHelper.showWarning(
                    requireContext(),
                    "Logout",
                    "Are you sure you want to log out?",
                    onConfirm = {

                        AuthServices(requireContext()).signOut()
                        val sharedPref =
                            requireActivity().getSharedPreferences(
                                "user_data",
                                AppCompatActivity.MODE_PRIVATE
                            )
                        sharedPref.edit {
                            clear()
                        }
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    },
                    onCancel = {

                    }
                )
            }

        }

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
