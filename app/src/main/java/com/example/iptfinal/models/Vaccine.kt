package com.example.iptfinal.models

data class Vaccine(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var route: String? = null,
    var type: String? = null,
    var sideEffects: String? = null,
    var eligibleAge: Int? = null,
    var ageUnit: String? = null,
    var schedule: String? = null,
    var hasDosage: Boolean = false
)