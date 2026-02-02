package com.example.sheshield.models

data class UserData(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val userType: String = "", // "user", "helper", "user_helper"
    val gender: String = "", // "male", "female", "other"
    val phone: String = "",
    val address: String = "",
    val isHelperVerified: Boolean = false,
    val helperLevel: Int = 1,
    val responseRadius: Int = 5,

    // --- NEW FIELDS FOR REAL-TIME TRACKING ---
    val isActive: Boolean = false,                 // Determines if helper shows on map
    val location: Map<String, Double>? = null,     // Stores { "latitude": x.x, "longitude": y.y }

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)