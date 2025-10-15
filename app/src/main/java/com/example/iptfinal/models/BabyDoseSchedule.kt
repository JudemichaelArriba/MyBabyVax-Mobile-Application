package com.example.iptfinal.models

data class BabyDoseSchedule(
    var doseName: String? = null,
    var interval: String? = null,
    var date: String? = null,
    var isCompleted: Boolean = false,
    var isVisible: Boolean = false
)
