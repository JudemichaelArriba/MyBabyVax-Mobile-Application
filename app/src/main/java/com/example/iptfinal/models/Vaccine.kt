package com.example.iptfinal.models

data class Vaccine(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var route: String? = null,
    var type: String? = null,
    var sideEffects: String? = null,
    var eligibleAge: Double? = null,
    var ageUnit: String? = null,
    var hasDosage: Boolean = false
)
