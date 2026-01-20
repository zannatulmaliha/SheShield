package com.example.sheshield.models

data class Alert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val alertType: String = "SOS", // Set default to SOS
    val riskLevel: String = "high", // Set default to high
    val status: String = "active", // Set default to active
    val location: LocationData = LocationData(),
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val acceptedBy: String? = null,
    val acceptedAt: Long? = null
)

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)