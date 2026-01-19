package com.example.sheshield.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sheshield.model.DangerZone
import com.example.sheshield.model.RiskLevel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

object MockDangerData {
    val defaultLocation = LatLng(23.7937, 90.4066)

    val zones = listOf(
        DangerZone(
            id = "h1",
            center = LatLng(23.9485, 90.3824),
            radiusMeters = 800.0,
            riskLevel = RiskLevel.HIGH,
            description = "Poor lighting and industrial traffic."
        ),
        DangerZone(
            id = "h2",
            center = LatLng(23.7508, 90.3934),
            radiusMeters = 600.0,
            riskLevel = RiskLevel.HIGH,
            description = "Multiple reports of harassment in late hours."
        ),
        DangerZone(
            id = "c1",
            center = LatLng(23.8892, 90.3934),
            radiusMeters = 500.0,
            riskLevel = RiskLevel.CAUTION,
            description = "Heavy traffic congestion and low visibility."
        ),
        DangerZone(
            id = "c2",
            center = LatLng(23.7335, 90.4116),
            radiusMeters = 700.0,
            riskLevel = RiskLevel.CAUTION,
            description = "Large crowds, keep personal belongings safe."
        ),
        DangerZone(
            id = "s1",
            center = LatLng(23.7986, 90.4215),
            radiusMeters = 900.0,
            riskLevel = RiskLevel.SAFE,
            description = "Heavy security and police patrol present."
        ),
        DangerZone(
            id = "s2",
            center = LatLng(23.7461, 90.3742),
            radiusMeters = 600.0,
            riskLevel = RiskLevel.SAFE,
            description = "Verified safe for pedestrians during day hours."
        )
    )
}

@Composable
fun GeneralMapScreen() {
    val context = LocalContext.current
    
    var currentRiskStatus by remember { mutableStateOf<DangerZone?>(null) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MockDangerData.defaultLocation, 13f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasLocationPermission = it
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(loc.latitude, loc.longitude), 15f
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(cameraPositionState.position) {
        val target = cameraPositionState.position.target
        val zone = MockDangerData.zones.find { zone ->
            calculateDistance(target, zone.center) <= zone.radiusMeters
        }
        
        if (zone != currentRiskStatus && zone?.riskLevel == RiskLevel.HIGH) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
            Toast.makeText(context, "Warning: Entering High Risk Zone", Toast.LENGTH_SHORT).show()
        }

        currentRiskStatus = zone
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.NORMAL
            )
        ) {
            MockDangerData.zones.forEach { zone ->
                Circle(
                    center = zone.center,
                    radius = zone.radiusMeters,
                    fillColor = zone.riskLevel.color,
                    strokeColor = zone.riskLevel.color.copy(alpha = 1f),
                    strokeWidth = 2f
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { 
                Toast.makeText(context, "Calculating route to the nearest Safe Zone...", Toast.LENGTH_SHORT).show()
            },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            text = { Text("Navigate to Safe Zone") },
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp)
        )

        SafetyStatusCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp, 16.dp, 16.dp, 90.dp),
            zone = currentRiskStatus
        )
    }
}

@Composable
fun SafetyStatusCard(modifier: Modifier = Modifier, zone: DangerZone?) {
    if (zone == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = zone.riskLevel.color.copy(alpha = 1f),
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = zone.riskLevel.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = zone.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun calculateDistance(start: LatLng, end: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0].toDouble()
}