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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)