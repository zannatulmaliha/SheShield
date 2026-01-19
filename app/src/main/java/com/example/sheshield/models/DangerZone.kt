package com.example.sheshield.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

enum class RiskLevel(val color: Color, val label: String) {
    SAFE(Color(0x444CAF50), "Safe Zone"),       // Transparent Green
    CAUTION(Color(0x44FFC107), "Caution Zone"), // Transparent Yellow
    HIGH(Color(0x44F44336), "High Risk Zone")   // Transparent Red
}

data class DangerZone(
    val id: String,
    val center: LatLng,
    val radiusMeters: Double,
    val riskLevel: RiskLevel,
    val description: String 
)