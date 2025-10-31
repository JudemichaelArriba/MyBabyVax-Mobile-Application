package com.example.iptfinal.services

import android.R
import android.hardware.Camera
import android.util.Log
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.models.Baby
import com.example.iptfinal.models.BabyDoseSchedule
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.models.BabyVaccineHistory
import com.example.iptfinal.models.BabyVaccineSchedule
import com.example.iptfinal.models.Users
import com.example.iptfinal.models.Vaccine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

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
        val updates = mutableMapOf<String, Any?>()

        updates["firstname"] = updatedUser.firstname
        updates["lastname"] = updatedUser.lastname
        updates["email"] = updatedUser.email
        updates["address"] = updatedUser.address
        updates["mobileNum"] = updatedUser.mobileNum
        updates["profilePic"] = updatedUser.profilePic

        databaseUsers.child(uid).updateChildren(updates).addOnSuccessListener {
            callback.onSuccess("Profile updated successfully")
        }.addOnFailureListener { e ->
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

        val scheduleMap = schedules.associateBy {
            it.vaccineName ?: "vaccine_${System.currentTimeMillis()}"
        }


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
            "createdAt" to baby.createdAt
        )


        newBabyRef.updateChildren(babyData)
            .addOnSuccessListener {

                newBabyRef.child("schedules").updateChildren(scheduleMap)
                    .addOnSuccessListener {
                        callback.onSuccess("Baby added successfully with schedules.")
                    }
                    .addOnFailureListener { e ->
                        callback.onError("Failed to add schedules: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback.onError("Failed to add baby: ${e.message}")
            }
    }


    fun fetchBabiesForUser(userId: String, callback: InterfaceClass.BabiesCallback) {
        val babiesRef = databaseUsers.child(userId).child("babies")
        babiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val babyList = mutableListOf<Baby>()
                for (babySnap in snapshot.children) {
                    val baby = babySnap.getValue(Baby::class.java)
                    if (baby != null) {
                        baby.id = babySnap.key
                        babyList.add(baby)
                        Log.d("DatabaseService", "Baby found: ${baby.fullName}")
                    }
                }
                callback.onBabiesLoaded(babyList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching babies: ${error.message}")
                callback.onError(error.message)
            }
        })
    }


    fun fetchAllBabyVaccineSchedules(
        userId: String, callback: InterfaceClass.BabyVaccineDisplayCallback
    ) {
        val userBabiesRef = databaseUsers.child(userId).child("babies")

        userBabiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vaccineDisplayList = mutableListOf<BabyVaccineDisplay>()

                for (babySnap in snapshot.children) {
                    val baby = babySnap.getValue(Baby::class.java)
                    if (baby == null) continue

                    val babyId = babySnap.key ?: continue
                    val schedulesSnap = babySnap.child("schedules")

                    for (vaccineSnap in schedulesSnap.children) {
                        val vaccine = vaccineSnap.getValue(BabyVaccineSchedule::class.java)
                        if (vaccine == null) continue

                        val dosesSnap = vaccineSnap.child("doses")
                        for (doseSnap in dosesSnap.children) {
                            val dose = doseSnap.getValue(BabyDoseSchedule::class.java)
                            if (dose != null && dose.isVisible) {
                                val display = BabyVaccineDisplay(
                                    babyId = babyId,
                                    babyName = baby.fullName,
                                    vaccineName = vaccine.vaccineName,
                                    vaccineType = vaccine.vaccineType,
                                    description = vaccine.description,
                                    route = vaccine.route,
                                    sideEffects = vaccine.sideEffects,
                                    doseName = dose.doseName,
                                    scheduleDate = dose.date,
                                    isCompleted = dose.isCompleted
                                )
                                vaccineDisplayList.add(display)
                            }
                        }
                    }
                }

                callback.onSchedulesLoaded(vaccineDisplayList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }

    fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                val map = mapOf("fcmToken" to token)
                userRef.updateChildren(map)
            }
        }
    }


    fun fetchBabyById(babyId: String, callback: InterfaceClass.BabyCallback) {
        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundBaby: Baby? = null

                for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == babyId) {
                            foundBaby = babySnap.getValue(Baby::class.java)
                            break
                        }
                    }
                    if (foundBaby != null) break
                }

                if (foundBaby != null) {
                    callback.onBabyLoaded(foundBaby)
                } else {
                    callback.onError("Baby not found with ID: $babyId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }


    fun markDoseAsCompleted(
        babyId: String,
        vaccineName: String,
        doseName: String,
        callback: InterfaceClass.StatusCallback
    ) {
        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var doseFound = false

                for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == babyId) {
                            val vaccineSnap = babySnap.child("schedules").child(vaccineName)
                            val dosesSnap = vaccineSnap.child("doses")

                            val doseChildren = dosesSnap.children.toList()
                            for ((index, doseChild) in doseChildren.withIndex()) {
                                val currentDoseName =
                                    doseChild.child("doseName").getValue(String::class.java)

                                if (currentDoseName == doseName) {

                                    val doseRef = doseChild.ref.child("completed")
                                    doseRef.setValue(true).addOnSuccessListener {

                                        if (index + 1 < doseChildren.size) {
                                            val nextDose = doseChildren[index + 1]
                                            val nextDoseRef = nextDose.ref.child("visible")
                                            nextDoseRef.setValue(true)
                                        }
                                        callback.onSuccess("Dose marked as completed.")
                                    }.addOnFailureListener { e ->
                                        callback.onError("Failed to update dose: ${e.message}")
                                    }

                                    doseFound = true
                                    break
                                }
                            }
                        }
                        if (doseFound) break
                    }
                    if (doseFound) break
                }

                if (!doseFound) {
                    callback.onError("Dose not found for baby ID: $babyId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError("Database error: ${error.message}")
            }
        })
    }


    fun clearFcmTokenOnLogout() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseDatabase.getInstance().getReference("users")
        db.child(user.uid).child("fcmToken").removeValue()
    }


    fun updateBaby(
        babyId: String, updatedBaby: Baby, callback: InterfaceClass.StatusCallback
    ) {

        val parentId = updatedBaby.parentId ?: return callback.onError("Parent ID is missing")
        val babyRef =
            FirebaseDatabase.getInstance().getReference("users").child(parentId).child("babies")
                .child(babyId)


        val updates = mutableMapOf<String, Any?>()
        updatedBaby.fullName?.let { updates["fullName"] = it }
        updatedBaby.gender?.let { updates["gender"] = it }
        updatedBaby.dateOfBirth?.let { updates["dateOfBirth"] = it }
        updatedBaby.birthPlace?.let { updates["birthPlace"] = it }
        updatedBaby.weightAtBirth?.let { updates["weightAtBirth"] = it }
        updatedBaby.heightAtBirth?.let { updates["heightAtBirth"] = it }
        updatedBaby.bloodType?.let { updates["bloodType"] = it }
        updatedBaby.profileImageUrl?.let { updates["profileImageUrl"] = it }


        babyRef.updateChildren(updates).addOnSuccessListener {
            callback.onSuccess("Baby info updated successfully.")
        }.addOnFailureListener { e ->
            callback.onError("Failed to update baby: ${e.message}")
        }
    }


    fun deleteBaby(babyId: String, callback: InterfaceClass.StatusCallback) {
        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var babyFound = false

                for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == babyId) {
                            babySnap.ref.removeValue().addOnSuccessListener {
                                callback.onSuccess("Baby deleted successfully.")
                            }.addOnFailureListener { e ->
                                callback.onError("Failed to delete baby: ${e.message}")
                            }
                            babyFound = true
                            break
                        }
                    }
                    if (babyFound) break
                }

                if (!babyFound) {
                    callback.onError("Baby not found with ID: $babyId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError("Database error: ${error.message}")
            }
        })
    }


    fun addVaccineHistory(
        baby: Baby,
        vaccineName: String,
        doseName: String,
        date: String,
        callback: InterfaceClass.StatusCallback
    ) {


        val history = BabyVaccineHistory(
            babyFullName = baby.fullName,
            babyGender = baby.gender,
            babyBloodType = baby.bloodType,
            babyDateOfBirth = baby.dateOfBirth,
            vaccineName = vaccineName,
            doseName = doseName,
            date = date

        )

        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var foundBaby = false

                for (userSnap in snapshot.children) {
                    val babiesSnapshot = userSnap.child("babies")
                    for (babySnap in babiesSnapshot.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == baby.id) {
                            val historyRef = babySnap.ref.child("history").push()
                            historyRef.setValue(history).addOnSuccessListener {
                                callback.onSuccess("History successfully addded")
                            }.addOnFailureListener { e ->
                                callback.onError("Failed to add history" + e.message)
                            }
                            foundBaby = true
                            break
                        }
                        if (foundBaby) break
                    }
                    if (!foundBaby) {
                        callback.onError("Baby not found with id" + baby.id)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError("database error something wen wrong" + error.message)
            }

        })

    }


    fun fetchAllBabiesHistoryForUser(
        userId: String,
        callback: (Map<String, List<BabyVaccineHistory>>) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        databaseUsers.child(userId).child("babies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historyMap = mutableMapOf<String, MutableList<BabyVaccineHistory>>()


                    for (babySnap in snapshot.children) {
                        val babyName =
                            babySnap.child("fullName").getValue(String::class.java) ?: "unknown"
                        val babyHistoryList = mutableListOf<BabyVaccineHistory>()
                        val historySnap = babySnap.child("history")
                        for (historyEntrySnap in historySnap.children) {
                            val history = historyEntrySnap.getValue(BabyVaccineHistory::class.java)
                            if (history != null) {
                                babyHistoryList.add(history)
                            }
                        }
                        historyMap[babyName] = babyHistoryList

                    }
                    callback(historyMap)

                }

                override fun onCancelled(error: DatabaseError) {
                    errorCallback(error.message)
                }

            })


    }


    fun updateNotificationPreference(
        userId: String,
        isEnabled: Boolean,
        callback: InterfaceClass.StatusCallback
    ) {
        val userRef = databaseUsers.child(userId).child("notifications_enabled")
        userRef.setValue(isEnabled)
            .addOnSuccessListener {
                Log.d("DatabaseService", "Notification preference saved: $isEnabled")
                callback.onSuccess("Notification preference updated.")
            }
            .addOnFailureListener { e ->
                Log.e("DatabaseService", "Failed to update notification preference: ${e.message}")
                callback.onError("Failed to update notification preference: ${e.message}")
            }
    }


//    fun fetchNotificationPreference(
//        userId: String,
//        callback: (Boolean) -> Unit,
//        errorCallback: (String) -> Unit
//    ) {
//        val userRef = databaseUsers.child(userId).child("notificationsEnabled")
//        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val isEnabled = snapshot.getValue(Boolean::class.java) ?: true
//                Log.d("DatabaseService", "Fetched notification preference: $isEnabled")
//                callback(isEnabled)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                errorCallback("Failed to fetch notification preference: ${error.message}")
//            }
//        })
//    }


    fun fetchVaccinesNotInBabySchedule(
        babyId: String,
        callback: InterfaceClass.VaccineCallback
    ) {
        databaseVaccines.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(vaccineSnapshot: DataSnapshot) {
                val allVaccines = mutableListOf<Vaccine>()
                for (vaccineSnap in vaccineSnapshot.children) {
                    val vaccine = vaccineSnap.getValue(Vaccine::class.java)
                    if (vaccine != null) {
                        vaccine.id = vaccineSnap.key ?: ""
                        allVaccines.add(vaccine)
                    }
                }


                databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        var scheduledVaccineNames: Set<String> = emptySet()

                        for (userSnap in userSnapshot.children) {
                            val babiesSnap = userSnap.child("babies")
                            for (babySnap in babiesSnap.children) {
                                val id = babySnap.child("id").getValue(String::class.java)
                                if (id == babyId) {
                                    val schedulesSnap = babySnap.child("schedules")
                                    scheduledVaccineNames =
                                        schedulesSnap.children.mapNotNull { it.key }.toSet()
                                    break
                                }
                            }
                            if (scheduledVaccineNames.isNotEmpty()) break
                        }


                        val notScheduledVaccines =
                            allVaccines.filter { it.name !in scheduledVaccineNames }
                        callback.onVaccinesLoaded(notScheduledVaccines)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback.onError("Failed to fetch baby's schedule: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError("Failed to fetch vaccines: ${error.message}")
            }
        })
    }


    fun addSchedulesToExistingBaby(
        babyId: String,
        newSchedules: List<BabyVaccineSchedule>,
        callback: InterfaceClass.StatusCallback
    ) {

        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var babyRefFound: DatabaseReference? = null

                loop@ for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == babyId) {
                            babyRefFound = babySnap.ref.child("schedules")
                            break@loop
                        }
                    }
                }

                if (babyRefFound == null) {
                    callback.onError("Baby not found with ID: $babyId")
                    return
                }

                val scheduleMap = newSchedules.associateBy {
                    it.vaccineName ?: "vaccine_${System.currentTimeMillis()}"
                }


                babyRefFound.updateChildren(scheduleMap)
                    .addOnSuccessListener {
                        callback.onSuccess("Schedules added successfully!")
                    }
                    .addOnFailureListener { e ->
                        callback.onError("Failed to add schedules: ${e.message}")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError("Database error: ${error.message}")
            }
        })
    }


}
