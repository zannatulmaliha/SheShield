package com.example.sheshield.models

data class Alert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val alertType: String = "", // "emergency", "check_in", "sos"
    val riskLevel: String = "", // "high", "medium", "low"
    val status: String = "", // "pending", "accepted", "en_route", "arrived", "resolved"
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