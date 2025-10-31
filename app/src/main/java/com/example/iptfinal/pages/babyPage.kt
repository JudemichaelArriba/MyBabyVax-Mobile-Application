package com.example.iptfinal.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.R
import com.example.iptfinal.adapters.BabyAdapter
import com.example.iptfinal.databinding.FragmentBabyPageBinding
import com.example.iptfinal.models.Baby
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class babyPage : Fragment() {

    private var _binding: FragmentBabyPageBinding? = null
    private val binding get() = _binding!!

    private var isExpanded = false
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupAddButton()
        setupSwipeRefresh()

        loadBabies()
    }

    private fun setupRecyclerView() {
        babyAdapter = BabyAdapter()
        binding.babyRecyclerView.apply {
            adapter = babyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchBar() {
        val searchContainer = binding.searchContainer
        val searchIcon = binding.searchIcon
        val searchInput = binding.searchInput

        searchIcon.setOnClickListener {
            if (!isExpanded) {
                expandSearchBar(searchContainer)
                searchInput.visibility = View.VISIBLE
                searchInput.alpha = 0f
                searchInput.animate().alpha(1f).setDuration(200).start()
            } else {
                collapseSearchBar(searchContainer)
                searchInput.animate().alpha(0f).setDuration(200)
                    .withEndAction { searchInput.visibility = View.GONE }
                    .start()
            }
            isExpanded = !isExpanded
        }
    }

    private fun setupAddButton() {
        binding.btnAddBaby.setOnClickListener {
            startActivity(Intent(requireContext(), AddBabyPage::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.mainColor))
        binding.swipeRefresh.setOnRefreshListener {
            loadBabies()
        }
    }

    private fun loadBabies() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.loading.visibility = View.VISIBLE
        binding.babyRecyclerView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val babies = fetchBabies(userId)
                babyAdapter.submitList(babies)
                isDataLoaded = true
            } catch (e: Exception) {
                Log.e("BabyPage", "Error fetching babies: ${e.message}")
            } finally {
                binding.loading.visibility = View.GONE
                binding.babyRecyclerView.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private suspend fun fetchBabies(userId: String): List<Baby> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchBabiesForUser(userId, object : InterfaceClass.BabiesCallback {
                override fun onBabiesLoaded(babies: List<Baby>) {
                    if (cont.isActive) cont.resume(babies)
                }

                override fun onError(message: String?) {
                    if (cont.isActive) cont.resumeWithException(Exception(message ?: "Error"))
                }
            })
        }

    private fun expandSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 600

        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    private fun collapseSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 120

        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

