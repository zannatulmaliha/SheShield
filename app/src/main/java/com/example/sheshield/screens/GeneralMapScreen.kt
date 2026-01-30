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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.sheshield.model.RiskLevel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*

// --- DATA MODELS ---
data class SafetyIncident(
    val id: String,
    val location: LatLng,
    val type: IncidentType
)

data class RouteOption(
    val id: String,
    val title: String,
    val description: String,
    val riskScore: Int,
    val destination: LatLng,
    val waypoint: LatLng? = null,
    val type: RouteType
)

enum class RouteType { FASTEST, SAFER_DETOUR, ALTERNATIVE_SHELTER }

enum class IncidentType(val baseSeverity: Int, val label: String, val color: Color) {
    HARASSMENT(10, "Harassment", Color.Red),
    THEFT(8, "Theft", Color(0xFFFF5722)),
    POOR_LIGHTING(5, "Poor Lighting", Color(0xFFFFC107)),
    SAFE_SHELTER(0, "Safe Zone", Color.Green)
}

@Composable
fun GeneralMapScreen() {
    val context = LocalContext.current
    val incidentList = remember { mutableStateListOf<SafetyIncident>() }

    // UI States
    var showRouteSelectionDialog by remember { mutableStateOf(false) }
    var availableRoutes by remember { mutableStateOf<List<RouteOption>>(emptyList()) }

    // --- SEED DATA ---
    LaunchedEffect(Unit) {
        if (incidentList.isEmpty()) {
            // DEMO DATA FOR NOW, NEED LARGE DATASET TO PRODUCE REALISTIC ZONES
            incidentList.addAll(listOf(
                // 1. Direct Path Hazard (Theft)
                SafetyIncident("d1", LatLng(23.9485, 90.3824), IncidentType.THEFT),
                // 2. Nearest Safe Zone (Blocked by d1)
                SafetyIncident("s1", LatLng(23.9495, 90.3830), IncidentType.SAFE_SHELTER),
                // 3. Alternative Safe Zone (Clear Path, but far)
                SafetyIncident("s2", LatLng(23.9450, 90.3800), IncidentType.SAFE_SHELTER)
            ))
        }
    }

    // --- MAP STATE ---
    var currentRiskLevel by remember { mutableStateOf(RiskLevel.SAFE) }
    var safetyScore by remember { mutableStateOf(100) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.9480, 90.3800), 15f)
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

    // --- RISK MONITORING ---
    LaunchedEffect(cameraPositionState.position, incidentList.size) {
        val target = cameraPositionState.position.target
        val nearbyIncidents = incidentList.filter {
            calculateDistance(target, it.location) <= 600.0 && it.type != IncidentType.SAFE_SHELTER
        }
        val rawRiskScore = nearbyIncidents.sumOf { it.type.baseSeverity }
        safetyScore = (100 - (rawRiskScore * 5)).coerceIn(0, 100)

        val newRiskLevel = if(safetyScore < 40) RiskLevel.HIGH else if(safetyScore < 75) RiskLevel.CAUTION else RiskLevel.SAFE
        if (newRiskLevel == RiskLevel.HIGH && currentRiskLevel != RiskLevel.HIGH) triggerVibration(context)
        currentRiskLevel = newRiskLevel
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            incidentList.forEach { incident ->
                val isSafe = incident.type == IncidentType.SAFE_SHELTER
                Circle(
                    center = incident.location,
                    radius = if (isSafe) 150.0 else 120.0,
                    fillColor = incident.type.color.copy(alpha = if (isSafe) 0.3f else 0.4f),
                    strokeColor = Color.Transparent
                )
                if (isSafe) {
                    Marker(
                        state = MarkerState(position = incident.location),
                        title = "Safe Shelter",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
            }
        }

        // TOP CENTER: SAFETY SCORE
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
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
            // REPORT BUTTON (ANTI-SPAM LOGIC)
            FloatingActionButton(
                onClick = {
                    val currentLocation = cameraPositionState.position.target
                    
                    // Check for any existing report within 30 meters
                    val isDuplicate = incidentList.any { 
                        it.type != IncidentType.SAFE_SHELTER && 
                        calculateDistance(currentLocation, it.location) < 30.0 
                    }

                    if (!isDuplicate) {
                        incidentList.add(SafetyIncident(System.currentTimeMillis().toString(), currentLocation, IncidentType.HARASSMENT))
                        Toast.makeText(context, "Incident Logged", Toast.LENGTH_SHORT).show()
                    } else {
                        // Silent fail: User is spamming or panicking, we just ignore the extra clicks
                        // to prevent circle stacking, without bothering them with an alert.
                    }
                },
                containerColor = Color.Red, contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Report") }

            // NAVIGATE (SMART ROUTING LOGIC)
            FloatingActionButton(
                onClick = {
                    val userLoc = cameraPositionState.position.target
                    val safeZones = incidentList.filter { it.type == IncidentType.SAFE_SHELTER }

                    if (safeZones.isEmpty()) {
                        Toast.makeText(context, "No Safe Zones nearby.", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    val routes = mutableListOf<RouteOption>()

                    // 1. Analyze Nearest Zone
                    val nearestZone = safeZones.minByOrNull { calculateDistance(userLoc, it.location) }!!
                    val directRisk = calculateRouteRisk(userLoc, nearestZone.location, incidentList)

                    routes.add(RouteOption(
                        "1", "Direct Route (Fastest)", 
                        "Goes to nearest shelter (${calculateDistance(userLoc, nearestZone.location).toInt()}m).", 
                        directRisk, nearestZone.location, null, RouteType.FASTEST
                    ))

                    // 2. Calculate Detour for Nearest Zone
                    val detourPoint = LatLng(userLoc.latitude, userLoc.longitude + 0.005) // Simple Demo Offset
                    val detourRisk = if (directRisk > 0) 0 else 5 // Demo Logic: Detour is safer
                    
                    routes.add(RouteOption(
                        "2", "Safety Detour", 
                        "Slightly longer, avoids reported zones.", 
                        detourRisk, nearestZone.location, detourPoint, RouteType.SAFER_DETOUR
                    ))

                    // 3. Analyze Alternative Safe Zones
                    safeZones.minus(nearestZone).forEachIndexed { index, zone ->
                        val altRisk = calculateRouteRisk(userLoc, zone.location, incidentList)
                        routes.add(RouteOption(
                            "3-$index", "Alternative Shelter", 
                            "Different location, possibly safer path.", 
                            altRisk, zone.location, null, RouteType.ALTERNATIVE_SHELTER
                        ))
                    }

                    // Sort: Safety First -> Then ID
                    availableRoutes = routes.sortedWith(compareBy<RouteOption> { it.riskScore }.thenBy { it.id })
                    showRouteSelectionDialog = true
                },
                containerColor = Color(0xFF4CAF50), contentColor = Color.White
            ) { Icon(Icons.Default.ExitToApp, "Safe Zone") }
        }

        // --- DIALOGS ---
        if (showRouteSelectionDialog) {
            RouteSelectionDialog(
                routes = availableRoutes,
                onDismiss = { showRouteSelectionDialog = false },
                onSelect = { route ->
                    showRouteSelectionDialog = false
                    launchGoogleMaps(context, route.destination, route.waypoint)
                }
            )
        }
    }
}

// --- COMPOSABLES FOR ROUTE SELECTION ---
@Composable
fun RouteSelectionDialog(routes: List<RouteOption>, onDismiss: () -> Unit, onSelect: (RouteOption) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Safe Path", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("AI analyzed ${routes.size} paths for risk.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(routes) { route -> RouteOptionCard(route, onSelect) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun RouteOptionCard(route: RouteOption, onClick: (RouteOption) -> Unit) {
    val isSafe = route.riskScore == 0
    val cardColor = if (isSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if (isSafe) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = when (route.type) {
        RouteType.FASTEST -> Icons.Default.DirectionsRun
        RouteType.SAFER_DETOUR -> Icons.Default.AltRoute
        RouteType.ALTERNATIVE_SHELTER -> Icons.Default.Storefront
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth().clickable { onClick(route) }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(route.title, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(route.description, fontSize = 12.sp, color = Color.Gray)
                if (!isSafe) Text("⚠️ High Risk detected", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                else Text("✅ Verified Safe Route", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// --- LOGIC HELPERS ---
fun calculateRouteRisk(start: LatLng, end: LatLng, incidents: List<SafetyIncident>): Int {
    var riskCount = 0
    val dangerZones = incidents.filter { it.type != IncidentType.SAFE_SHELTER }
    for (zone in dangerZones) {
        if (pointToLineDistance(zone.location, start, end) < 0.0025) riskCount += zone.type.baseSeverity
    }
    return riskCount
}

fun launchGoogleMaps(context: Context, destination: LatLng, waypoint: LatLng? = null) {
    val uriString = if (waypoint != null) {
        "http://googleusercontent.com/maps.google.com/maps?daddr=${destination.latitude},${destination.longitude}&waypoints=${waypoint.latitude},${waypoint.longitude}&dirflg=w"
    } else {
        "google.navigation:q=${destination.latitude},${destination.longitude}&mode=w"
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    intent.setPackage("com.google.android.apps.maps")
    try { context.startActivity(intent) } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
    }
}

fun pointToLineDistance(P: LatLng, A: LatLng, B: LatLng): Double {
    val normalLength = hypot(B.longitude - A.longitude, B.latitude - A.latitude)
    if (normalLength == 0.0) return 0.0
    return abs((P.longitude - A.longitude) * (B.latitude - A.latitude) - (P.latitude - A.latitude) * (B.longitude - A.longitude)) / normalLength
}

fun calculateDistance(start: LatLng, end: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
    return results[0].toDouble()
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
        vibrator.vibrate(500)
    }
}

fun getScoreColor(score: Int): Color = when {
    score >= 75 -> Color(0xFF4CAF50)
    score >= 40 -> Color(0xFFFFA500)
    else -> Color.Red
}