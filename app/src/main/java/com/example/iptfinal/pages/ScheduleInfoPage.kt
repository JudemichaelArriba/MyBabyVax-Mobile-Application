package com.example.iptfinal.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.R
import com.example.iptfinal.adapters.BabyAdapter
import com.example.iptfinal.components.DialogHelper
import com.example.iptfinal.components.bottomNav
import com.example.iptfinal.databinding.ActivityScheduleInfoPageBinding
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Baby
import com.example.iptfinal.services.DatabaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalGetImage::class)
class ScheduleInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleInfoPageBinding
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()

    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor by lazy { ContextCompat.getMainExecutor(this) }

    private var currentBabyId: String? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.mainColor)


        val babyId = intent.getStringExtra("babyId")
        val vaccineName = intent.getStringExtra("vaccineName")
        val doseName = intent.getStringExtra("doseName")
        val scheduleDate = intent.getStringExtra("scheduleDate")
        val vaccineType = intent.getStringExtra("vaccineType")
        val route = intent.getStringExtra("route")
        val description = intent.getStringExtra("description")
        val sideEffects = intent.getStringExtra("sideEffects")


        binding.vaccineName.text = vaccineName
        binding.doseName.text = doseName
        binding.vaccineType.text = vaccineType
        binding.route.text = route
        binding.scheduleDate.text = scheduleDate
        binding.description.text = description
        binding.sideEffects.text = sideEffects

        binding.backButton.setOnClickListener { finish() }

        babyAdapter = BabyAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ScheduleInfoPage)
            adapter = babyAdapter
        }


        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && babyId != null) {
            currentBabyId = babyId

            fetchBabyByIdCoroutine(babyId)
        }


        binding.btnGenerateQR.setOnClickListener {
            if (allPermissionsGranted()) {
                startCameraPreview()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCameraPreview()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.qrCode.surfaceProvider)

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, analyzer
                )

            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }, cameraExecutor)
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        runOnUiThread {
                            binding.darkOverlay.visibility = View.VISIBLE
                            binding.qrLoading.visibility = View.VISIBLE
                        }
                        val qrData = barcodes.first().rawValue
                        if (qrData != null) {
                            handleQRData(qrData)
                            cameraProvider?.unbindAll()
                        }
                        binding.qrCode.postDelayed({
                            binding.darkOverlay.visibility = View.GONE
                            binding.qrLoading.visibility = View.GONE
                        }, 1500)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QR Scan", "Failed: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun handleQRData(qrData: String) {
        try {
            val json = JSONObject(qrData)
            val vaccineName = json.getString("vaccineName")
            val doseName = json.getString("doseName")
            val date = json.getString("date")
            val babyIds = json.getJSONArray("babyIds")

            val babyList = mutableListOf<String>()
            for (i in 0 until babyIds.length()) {
                babyList.add(babyIds.getString(i))
            }

            runOnUiThread {
                binding.vaccineName.text = vaccineName
                binding.doseName.text = doseName
                binding.scheduleDate.text = date
            }

            if (currentBabyId != null && babyList.contains(currentBabyId)) {
                lifecycleScope.launchWhenStarted {
                    try {
                        withContext(Dispatchers.IO) {
                            markDoseAsCompletedSuspend(currentBabyId!!, vaccineName, doseName)
                            val baby = fetchBabySuspend(currentBabyId!!)

                            databaseService.addVaccineHistory(
                                baby,
                                vaccineName,
                                doseName,
                                date,
                                object : InterfaceClass.StatusCallback {
                                    override fun onSuccess(message: String) {
                                        Log.d("History", message)
                                    }

                                    override fun onError(message: String) {
                                        Log.e("History", message)
                                    }

                                }
                            )
                        }

                        withContext(Dispatchers.Main) {
                            DialogHelper.showSuccess(
                                this@ScheduleInfoPage,
                                "Successful",
                                "Successfully Scanned the Qrcode"
                            ) {
                                finish()
                            }


                        }


                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ScheduleInfoPage,
                                "${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {

                        binding.darkOverlay.visibility = View.GONE
                        binding.qrLoading.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this, "Invalid QR Code or baby ID mismatch.", Toast.LENGTH_LONG)
                    .show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Invalid QR data", Toast.LENGTH_SHORT).show()
        }
    }


    private suspend fun markDoseAsCompletedSuspend(
        babyId: String,
        vaccineName: String,
        doseName: String
    ): Unit = suspendCancellableCoroutine { cont ->
        databaseService.markDoseAsCompleted(
            babyId,
            vaccineName,
            doseName,
            object : InterfaceClass.StatusCallback {
                override fun onSuccess(message: String) {
                    cont.resume(Unit) {}
                }

                override fun onError(message: String) {
                    cont.resumeWithException(Exception(message))
                }
            })
    }


    private fun fetchBabyByIdCoroutine(babyId: String) {
        lifecycleScope.launchWhenStarted {
            binding.loading.visibility = View.VISIBLE
            try {
                val baby = withContext(Dispatchers.IO) {
                    fetchBabySuspend(babyId)
                }
                binding.loading.visibility = View.GONE
                babyAdapter.submitList(listOf(baby))
                binding.recyclerView.visibility = View.VISIBLE

            } catch (e: Exception) {
                binding.loading.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
            }
        }
    }

    private suspend fun fetchBabySuspend(babyId: String): Baby =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
                override fun onBabyLoaded(baby: Baby) {
                    cont.resume(baby)
                }

                override fun onError(message: String) {
                    cont.resumeWithException(Exception(message))
                }
            })
        }
}
