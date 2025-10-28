package com.example.iptfinal.pages

import android.view.View
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.iptfinal.R
import com.example.iptfinal.databinding.FragmentHomePageBinding
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.models.Users
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import com.example.iptfinal.interfaces.InterfaceClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.*
import com.example.iptfinal.adapters.ScheduleAdapter
import com.example.iptfinal.services.NotificationManagerHelper

class homePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private val databaseService = DatabaseService()
    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()
        val sessionManager = SessionManager(requireContext())
        val user: Users = sessionManager.getUser()


//        binding.notificationIcon.setOnClickListener {
//            NotificationManagerHelper.clearCount(requireContext())
//            updateNotificationBadge()
//
//        }

        val username = if (user.firstname.isNotEmpty() || user.lastname.isNotEmpty()) {
            "${user.firstname} ${user.lastname}"
        } else {
            "Username"
        }


        binding.username.text = username

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

        binding.history.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), HistoryPage::class.java))
        }

        binding.schedule.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), SchedulePage::class.java))
        }

        setupUpcomingRecycler()
        loadUpcomingSchedules()
    }

    private fun setupUpcomingRecycler() {
        binding.upcomingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadUpcomingSchedules() {
        val userId = auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {

                val safeBinding = _binding ?: return@launch

                safeBinding.loading.visibility = View.VISIBLE
                safeBinding.upcomingRecyclerView.visibility = View.GONE
                safeBinding.nestedScrollView.visibility = View.VISIBLE

                val schedules = fetchSchedules(userId)

                val sorted =
                    schedules.filter { !it.isCompleted && !it.scheduleDate.isNullOrEmpty() }
                        .sortedBy {
                            try {
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).parse(it.scheduleDate!!)
                            } catch (e: Exception) {
                                null
                            }
                        }.take(3)

                val adapter = ScheduleAdapter(sorted)
                safeBinding.upcomingRecyclerView.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val safeBinding = _binding ?: return@launch
                safeBinding.loading.visibility = View.GONE
                safeBinding.upcomingRecyclerView.visibility = View.VISIBLE
            }
        }
    }


    private suspend fun fetchSchedules(userId: String): List<BabyVaccineDisplay> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabyVaccineSchedules(
                userId,
                object : InterfaceClass.BabyVaccineDisplayCallback {
                    override fun onSchedulesLoaded(schedules: List<BabyVaccineDisplay>) {
                        cont.resume(schedules)
                    }

                    override fun onError(error: String) {
                        cont.resumeWithException(Exception(error))
                    }
                }
            )
        }


    private fun updateNotificationBadge() {
        val count = NotificationManagerHelper.getCount(requireContext())
        Log.d("notifdevug", "Notification count = $count")
        if (count > 0) {
            binding.notificationBadge.visibility = View.VISIBLE
            binding.notificationBadge.text = count.toString()
        } else {
            binding.notificationBadge.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }
}
