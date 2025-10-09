package com.example.iptfinal.models

data class Baby(
    var id: String? = null,
    var parentId: String? = null,
    var fullName: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var birthPlace: String? = null,
    var weightAtBirth: Double? = null,
    var heightAtBirth: Double? = null,
    var bloodType: String? = null,
    var guardianName: String? = null,
    var contactNumber: String? = null,
    var address: String? = null,
    var profileImageUrl: String? = null,
    var createdAt: Long? = null
)
