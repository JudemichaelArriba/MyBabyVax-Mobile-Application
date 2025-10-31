package com.example.iptfinal.pages

import android.view.View
import android.os.Bundle
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

class homePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private val databaseService = DatabaseService()
    private lateinit var auth: FirebaseAuth

    private var allSchedules: MutableList<BabyVaccineDisplay> = mutableListOf()
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val username = if (user.firstname.isNotEmpty() || user.lastname.isNotEmpty()) {
            "${user.firstname} ${user.lastname}"
        } else "Username"

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
        setupSwipeRefresh()

        if (!isDataLoaded) loadUpcomingSchedules()
    }

    private fun setupUpcomingRecycler() {
        binding.upcomingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        scheduleAdapter = ScheduleAdapter(allSchedules)
        binding.upcomingRecyclerView.adapter = scheduleAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.mainColor))
        binding.swipeRefresh.setOnRefreshListener {
            loadUpcomingSchedules()
        }
    }

    private fun loadUpcomingSchedules() {
        val userId = auth.currentUser?.uid ?: return
        val safeBinding = _binding ?: return

        safeBinding.loading.visibility = View.VISIBLE
        safeBinding.upcomingRecyclerView.visibility = View.GONE
        safeBinding.nestedScrollView.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                val schedules = fetchSchedules(userId)
                val sorted = schedules
                    .filter { !it.isCompleted && !it.scheduleDate.isNullOrEmpty() }
                    .sortedBy {
                        try {
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).parse(it.scheduleDate!!)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .take(3)

                allSchedules.clear()
                allSchedules.addAll(sorted)


                _binding?.upcomingRecyclerView?.adapter?.let { scheduleAdapter.notifyDataSetChanged() }

                isDataLoaded = true

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _binding?.let {
                    it.loading.visibility = View.GONE
                    it.upcomingRecyclerView.visibility = View.VISIBLE
                    it.swipeRefresh.isRefreshing = false
                }
            }
        }
    }


    private suspend fun fetchSchedules(userId: String): List<BabyVaccineDisplay> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabyVaccineSchedules(
                userId,
                object : InterfaceClass.BabyVaccineDisplayCallback {
                    override fun onSchedulesLoaded(schedules: List<BabyVaccineDisplay>) {
                        if (cont.isActive) cont.resume(schedules)
                    }

                    override fun onError(error: String) {
                        if (cont.isActive) cont.resumeWithException(Exception(error))
                    }
                }
            )
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
