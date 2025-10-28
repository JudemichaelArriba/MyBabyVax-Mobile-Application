package com.example.iptfinal.models

data class BabyVaccineHistory(
    var babyFullName: String? = null,
    var babyGender: String? = null,
    var babyDateOfBirth: String? = null,
    var babyBloodType: String? = null,
    var vaccineName: String? = null,
    var doseName: String? = null,
    var date: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)
