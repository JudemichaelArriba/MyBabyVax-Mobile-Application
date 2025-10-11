package com.example.iptfinal.services

import android.util.Log
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Baby
import com.example.iptfinal.models.BabyVaccineSchedule
import com.example.iptfinal.models.Users
import com.example.iptfinal.models.Vaccine
import com.google.firebase.database.*

class DatabaseService {

    private val databasePuroks: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Locations/Monkayo/Union")
    private val databaseUsers: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

    private val databaseVaccines: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("vaccines")
    private val databaseBabies: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("babies")

    fun fetchPuroks(callback: InterfaceClass.PurokCallback) {
        databasePuroks.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val purokList = mutableListOf<String>()
                for (purokSnap in snapshot.children) {
                    val purokName = purokSnap.getValue(String::class.java) ?: ""
                    purokList.add(purokName)
                    Log.d("DatabaseService", "Purok found: $purokName")
                }
                callback.onPuroksLoaded(purokList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching puroks: ${error.message}")
                callback.onError(error.message)
            }
        })
    }

    fun fetchUserById(uid: String, callback: InterfaceClass.UserCallback) {
        databaseUsers.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        callback.onUserLoaded(user)
                    } else {
                        callback.onError("User data is empty")
                    }
                } else {
                    callback.onError("User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching user: ${error.message}")
                callback.onError(error.message)
            }
        })
    }

    fun updateUser(uid: String, updatedUser: Users, callback: InterfaceClass.StatusCallback) {
        databaseUsers.child(uid).setValue(updatedUser)
            .addOnSuccessListener {
                callback.onSuccess("Profile updated successfully")
            }
            .addOnFailureListener { e ->
                callback.onError("Failed to update profile: ${e.message}")
            }
    }


    fun fetchVaccines(callback: InterfaceClass.VaccineCallback) {
        databaseVaccines.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vaccineList = mutableListOf<com.example.iptfinal.models.Vaccine>()
                for (vaccineSnap in snapshot.children) {
                    val vaccine =
                        vaccineSnap.getValue(com.example.iptfinal.models.Vaccine::class.java)
                    if (vaccine != null) {
                        vaccine.id = vaccineSnap.key ?: ""
                        vaccineList.add(vaccine)
                        Log.d("DatabaseService", "Vaccine found: ${vaccine.name}")
                    }
                }
                callback.onVaccinesLoaded(vaccineList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching vaccines: ${error.message}")
                callback.onError(error.message)
            }
        })
    }


    fun fetchDoses(vaccineId: String, callback: InterfaceClass.DoseCallback) {
        val dosesRef = databaseVaccines.child(vaccineId).child("doses")
        dosesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doseList = mutableListOf<com.example.iptfinal.models.Dose>()
                for (doseSnap in snapshot.children) {
                    val dose = doseSnap.getValue(com.example.iptfinal.models.Dose::class.java)
                    if (dose != null) {
                        dose.id = doseSnap.key ?: ""
                        doseList.add(dose)
                        Log.d("DatabaseService", "Dose found: ${dose.name}")
                    }
                }
                callback.onDosesLoaded(doseList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching doses: ${error.message}")
                callback.onError(error.message)
            }
        })
    }


    fun addBabyWithSchedule(
        userId: String,
        baby: Baby,
        schedules: List<BabyVaccineSchedule>,
        callback: InterfaceClass.StatusCallback
    ) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val userBabiesRef = rootRef.child("users").child(userId).child("babies")
        val newBabyRef = userBabiesRef.push()
        val babyId = newBabyRef.key ?: return callback.onError("Failed to generate baby ID.")

        baby.id = babyId
        baby.parentId = userId
        baby.createdAt = System.currentTimeMillis()

        val scheduleMap =
            schedules.associateBy { it.vaccineName ?: "vaccine_${System.currentTimeMillis()}" }

        val babyData = mapOf(
            "id" to baby.id,
            "fullName" to baby.fullName,
            "gender" to baby.gender,
            "dateOfBirth" to baby.dateOfBirth,
            "birthPlace" to baby.birthPlace,
            "weightAtBirth" to baby.weightAtBirth,
            "heightAtBirth" to baby.heightAtBirth,
            "bloodType" to baby.bloodType,
            "profileImageUrl" to baby.profileImageUrl,
            "parentId" to baby.parentId,
            "createdAt" to baby.createdAt,
            "schedules" to scheduleMap
        )

        newBabyRef.setValue(babyData)
            .addOnSuccessListener { callback.onSuccess("Baby added as a child of the user.") }
            .addOnFailureListener { e -> callback.onError("Failed to add baby: ${e.message}") }
    }


}
