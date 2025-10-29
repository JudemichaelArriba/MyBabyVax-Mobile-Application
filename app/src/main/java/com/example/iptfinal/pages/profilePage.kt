package com.example.iptfinal.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iptfinal.MainActivity
import com.example.iptfinal.R
import com.example.iptfinal.databinding.FragmentProfilePageBinding
import com.example.iptfinal.services.AuthServices
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.NotificationPreference
import com.example.iptfinal.services.SessionManager
import com.google.firebase.database.FirebaseDatabase

class profilePage : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!
    private val databaseService = DatabaseService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.white)

        val sessionManager = SessionManager(requireContext())
        val user = sessionManager.getUser()

        if (user != null) {
            binding.username.text = "${user.firstname} ${user.lastname}"
            binding.emailText.text = user.email

            Log.d("ProfilePage", "Loaded user: ${user.firstname} ${user.lastname}")

            // Load profile picture
            if (user.profilePic.isNotEmpty()) {
                try {
                    if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                        val imageBytes =
                            android.util.Base64.decode(user.profilePic, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                            imageBytes, 0, imageBytes.size
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
        } else {
            binding.username.text = "Guest"
            binding.emailText.text = "Not available"
        }


        binding.switchNotification.isChecked = NotificationPreference.isEnabled(requireContext())

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            NotificationPreference.setEnabled(requireContext(), isChecked)
            user?.uid?.let { uid ->
                databaseService.updateNotificationPreference(
                    uid,
                    isChecked,
                    object : InterfaceClass.StatusCallback {
                        override fun onSuccess(message: String) {
                            Log.d("ProfilePage", message)
                        }

                        override fun onError(error: String) {
                            Log.e("ProfilePage", error)
                        }
                    })
            }
        }

        binding.accountInfo.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoPage::class.java))
        }

        binding.card1.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoPage::class.java))
        }


        binding.logout.setOnClickListener {
            DialogHelper.showWarning(
                requireContext(),
                "Logout",
                "Are you sure you want to log out?",
                onConfirm = {
                    databaseService.clearFcmTokenOnLogout()
                    AuthServices(requireContext()).signOut()
                    sessionManager.clearSession()
                    NotificationPreference.clear(requireContext())
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                },
                onCancel = {}
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
