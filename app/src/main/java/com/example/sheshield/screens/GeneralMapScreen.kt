package com.example.sheshield.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.sheshield.R
import com.example.sheshield.model.RiskLevel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.*

// --- DATA MODELS ---
data class SafetyIncident(
    val id: String,
    val location: LatLng,
    val type: IncidentType,
    val description: String = "",
    val severityScore: Int = 0
)

data class RouteOption(
    val id: String,
    val title: String,
    val description: String,
    val riskScore: Int,
    val destination: LatLng,
    val type: RouteType,
    val reason: String // New field to explain the "Why"
)

enum class RouteType { FASTEST, SAFER_DETOUR, ALTERNATIVE_SHELTER }

enum class IncidentType(val baseSeverity: Int, val label: String, val color: Color) {
    ASSAULT(10, "Assault", Color(0xFFD32F2F)),   // Dark Red
    MUGGING(8, "Mugging", Color(0xFFFF9800)),    // Deep Orange
    HARASSMENT(7, "Harassment", Color.Red),
    THEFT(6, "Theft", Color(0xFFFF5722)),
    POOR_LIGHTING(4, "Poor Lighting", Color(0xFFFFC107)),
    SAFE_SHELTER(0, "Safe Zone", Color(0xFF00FF77)) // Bright Neon Green
}

@Composable
fun GeneralMapScreen() {
    val context = LocalContext.current
    val incidentList = remember { mutableStateListOf<SafetyIncident>() }

    // UI States
    var showRouteSelectionDialog by remember { mutableStateOf(false) }
    var availableRoutes by remember { mutableStateOf<List<RouteOption>>(emptyList()) }

    // --- HOVER STATE ---
    var hoveredZone by remember { mutableStateOf<SafetyIncident?>(null) }

    // --- DATA LOADING ---
    LaunchedEffect(Unit) {
        if (incidentList.isEmpty()) {
            val localData = loadLocalIncidents(context)
            incidentList.addAll(localData)
        }
    }

    // --- MAP STATE ---
    var safetyScore by remember { mutableStateOf(100) }

    // Camera centered near Gazipur/Boardbazar
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.9215, 90.4160), 13f)
    }

    // Permission Logic
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasLocationPermission = it }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            try {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f)
                    }
            } catch (e: Exception) { e.printStackTrace() }
        } else { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    }

    // --- RISK & HOVER MONITORING ---
    LaunchedEffect(cameraPositionState.position, incidentList.size) {
        val target = cameraPositionState.position.target

        // 1. Calculate Risk Score
        val nearbyIncidents = incidentList.filter {
            calculateDistance(target, it.location) <= 600.0 && it.type != IncidentType.SAFE_SHELTER
        }
        val rawRiskScore = nearbyIncidents.sumOf { it.severityScore }
        safetyScore = (100 - (rawRiskScore * 3)).coerceIn(0, 100)

        // 2. DETECT HOVER
        val zoneUnderCamera = incidentList.find { incident ->
            val isSafe = incident.type == IncidentType.SAFE_SHELTER
            val radius = if (isSafe) 500.0 else (incident.severityScore * 90.0).coerceAtLeast(500.0)
            calculateDistance(target, incident.location) <= radius
        }

        if (zoneUnderCamera != null && zoneUnderCamera != hoveredZone) {
            triggerVibration(context)
        }

        hoveredZone = zoneUnderCamera
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- ANIMATION ---
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Restart
            )
        )

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            incidentList.forEach { incident ->
                val isSafe = incident.type == IncidentType.SAFE_SHELTER
                val isHovered = hoveredZone == incident

                // Radius logic
                val areaRadius = if (isSafe) 500.0 else (incident.severityScore * 65.0).coerceAtLeast(500.0)

                // 1. AREA CIRCLE
                Circle(
                    center = incident.location,
                    radius = areaRadius,
                    fillColor = incident.type.color.copy(alpha = if (isSafe) 0.35f else 0.4f),
                    strokeColor = if(isHovered) Color.White else if(isSafe) Color.White else Color.Transparent,
                    strokeWidth = if(isHovered) 10f else if(isSafe) 8f else 0f
                )

                // 2. PULSE (Danger only)
                if (!isSafe) {
                    Circle(
                        center = incident.location,
                        radius = areaRadius * pulseScale,
                        fillColor = incident.type.color.copy(alpha = 0.15f),
                        strokeColor = Color.Transparent,
                    )
                }

                // 3. CENTER DOT
                Circle(
                    center = incident.location,
                    radius = 15.0,
                    fillColor = incident.type.color,
                    strokeColor = Color.White,
                    strokeWidth = 3f
                )
            }
        }

        // --- CENTER CROSSHAIR ---
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Center Focus",
            tint = Color.DarkGray.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Center).size(24.dp)
        )

        // --- FLOATING INFO WINDOW ---
        AnimatedVisibility(
            visible = hoveredZone != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            hoveredZone?.let { zone -> FloatingZoneInfoCard(zone) }
        }

        // TOP CENTER: SAFETY SCORE
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Text(
                text = "Safety Score: $safetyScore%",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                fontWeight = FontWeight.Bold,
                color = getScoreColor(safetyScore)
            )
        }

        // RIGHT SIDE: ACTION BUTTONS
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val userLoc = cameraPositionState.position.target
                    val safeZones = incidentList.filter { it.type == IncidentType.SAFE_SHELTER }

                    if (safeZones.isEmpty()) {
                        Toast.makeText(context, "No Safe Zones nearby.", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    // --- ROUTING LOGIC ---
                    val routes = mutableListOf<RouteOption>()
                    // Sort by distance to find closest ones
                    val nearbySafeZones = safeZones.sortedBy { calculateDistance(userLoc, it.location) }.take(3)

                    nearbySafeZones.forEachIndexed { index, zone ->
                        val risk = calculateRouteRisk(userLoc, zone.location, incidentList)
                        val dist = calculateDistance(userLoc, zone.location).toInt()

                        // Determine Route Type and Reason logic
                        val (type, reason) = when {
                            risk == 0 -> Pair(RouteType.SAFER_DETOUR, "Avoids all reported danger zones")
                            risk < 5 -> Pair(RouteType.FASTEST, "Fastest path, but passes near low risk area")
                            else -> Pair(RouteType.ALTERNATIVE_SHELTER, "Crosses known high-risk areas")
                        }

                        routes.add(RouteOption(
                            id = "route_$index",
                            title = if(risk == 0) "Safest Route" else "Fastest Route",
                            description = "${zone.description} ($dist m)",
                            riskScore = risk,
                            destination = zone.location,
                            type = type,
                            reason = reason
                        ))
                    }

                    // Ensure at least one safe option is highlighted if available, otherwise just show sorted
                    availableRoutes = routes.sortedBy { it.riskScore }
                    showRouteSelectionDialog = true
                },
                containerColor = Color(0xFF4CAF50), contentColor = Color.White
            ) { Icon(Icons.Default.ExitToApp, "Safe Zone") }
        }

        // --- DIALOG FOR GOOGLE MAPS LAUNCH ---
        if (showRouteSelectionDialog) {
            RouteSelectionDialog(
                routes = availableRoutes,
                onDismiss = { showRouteSelectionDialog = false },
                onSelect = { route ->
                    showRouteSelectionDialog = false
                    try {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${route.destination.latitude},${route.destination.longitude}&mode=w")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val browserUri = Uri.parse("http://googleusercontent.com/maps.google.com/maps?daddr=${route.destination.latitude},${route.destination.longitude}&travelmode=walking")
                            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                            context.startActivity(browserIntent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open Maps", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

// --- COMPOSABLES & HELPERS ---

@Composable
fun RouteSelectionDialog(routes: List<RouteOption>, onDismiss: () -> Unit, onSelect: (RouteOption) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Safe Path",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Routes analyzed using live crowd-sourced safety data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routes) { route -> RouteOptionCard(route, onSelect) }
                }
            }
        }
    }
}

@Composable
fun RouteOptionCard(route: RouteOption, onClick: (RouteOption) -> Unit) {
    val isSafe = route.riskScore == 0
    // Distinct Styling for Safe vs Risky
    val bgColor = if (isSafe) Color(0xFFE0F2F1) else Color(0xFFFFEBEE) // Light Teal vs Light Red
    val strokeColor = if (isSafe) Color(0xFF00695C) else Color(0xFFC62828) // Dark Teal vs Dark Red
    val iconVec = if (isSafe) Icons.Default.VerifiedUser else Icons.Default.Warning

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, strokeColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon
            Icon(
                imageVector = iconVec,
                contentDescription = null,
                tint = strokeColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Center: Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = route.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = strokeColor
                    )
                    if (isSafe) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RECOMMENDED", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = route.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Right: Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun FloatingZoneInfoCard(zone: SafetyIncident) {
    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(zone.type.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (zone.type == IncidentType.SAFE_SHELTER) Icons.Default.HealthAndSafety else Icons.Default.Warning,
                    contentDescription = null,
                    tint = zone.type.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = zone.type.label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = zone.type.color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = zone.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun loadLocalIncidents(context: Context): List<SafetyIncident> {
    val incidents = mutableListOf<SafetyIncident>()
    try {
        val inputStream = context.resources.openRawResource(R.raw.incidents)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val typeStr = obj.optString("type", "POOR_LIGHTING")
            val typeEnum = try { IncidentType.valueOf(typeStr) } catch (e: Exception) { IncidentType.POOR_LIGHTING }
            val severity = obj.optInt("severity", typeEnum.baseSeverity)

            incidents.add(SafetyIncident(
                id = obj.optString("id", "unknown"),
                location = LatLng(obj.getDouble("lat"), obj.getDouble("lng")),
                type = typeEnum,
                description = obj.optString("description", ""),
                severityScore = severity
            ))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return incidents
}

fun calculateDistance(start: LatLng, end: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
    return results[0].toDouble()
}

fun calculateRouteRisk(start: LatLng, end: LatLng, incidents: List<SafetyIncident>): Int {
    var riskCount = 0
    val dangerZones = incidents.filter { it.type != IncidentType.SAFE_SHELTER }
    for (zone in dangerZones) {
        if (pointToLineDistance(zone.location, start, end) < 0.0025) riskCount += zone.severityScore
    }
    return riskCount
}

fun pointToLineDistance(P: LatLng, A: LatLng, B: LatLng): Double {
    val normalLength = hypot(B.longitude - A.longitude, B.latitude - A.latitude)
    if (normalLength == 0.0) return 0.0
    return abs((P.longitude - A.longitude) * (B.latitude - A.latitude) - (P.latitude - A.latitude) * (B.longitude - A.longitude)) / normalLength
}

fun getScoreColor(score: Int): Color = when {
    score >= 75 -> Color(0xFF4CAF50)
    score >= 40 -> Color(0xFFFFA500)
    else -> Color.Red
}

fun triggerVibration(context: Context) {
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
        vibrator.vibrate(250)
    }
}
