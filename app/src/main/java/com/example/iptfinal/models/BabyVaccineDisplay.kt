package com.example.iptfinal.models

data class BabyVaccineDisplay(
    var babyId: String? = null,
    var babyName: String? = null,
    var vaccineName: String? = null,
    var vaccineType: String? = null,
    var description: String? = null,
    var route: String? = null,
    var sideEffects: String? = null,
    var doseName: String? = null,
    var scheduleDate: String? = null,
    var isCompleted: Boolean = false
)
