package com.example.iptfinal.models

data class BabyVaccineSchedule(
    var vaccineName: String? = null,
    var vaccineType: String? = null,
    var description: String? = null,
    var route: String? = null,
    var sideEffects: String? = null,
    var lastGiven: String? = null,
    var doses: List<BabyDoseSchedule>? = null
)
