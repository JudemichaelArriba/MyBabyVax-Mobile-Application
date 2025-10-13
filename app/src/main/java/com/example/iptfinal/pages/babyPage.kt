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


        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val babies = fetchBabies(currentUserId)
                    babyAdapter.submitList(babies)
                } catch (e: Exception) {
                    Log.e("BabyPage", "Error fetching babies: ${e.message}")
                }
            }
        }
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

    private suspend fun fetchBabies(userId: String): List<Baby> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchBabiesForUser(userId, object : InterfaceClass.BabiesCallback {
                override fun onBabiesLoaded(babies: List<Baby>) {
                    cont.resume(babies)
                }

                override fun onError(message: String?) {
                    cont.resumeWithException(Exception(message ?: "Error"))
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
