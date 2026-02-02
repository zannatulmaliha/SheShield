package com.example.sheshield.screens.helper

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.sheshield.models.Alert
import com.example.sheshield.models.UserData
import com.example.sheshield.navigation.HelperScreen
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperDashboard(
    onNavigate: (HelperScreen) -> Unit,
    onSwitchToUserMode: (() -> Unit)?,
    onAcceptAlert: (Alert) -> Unit,
    userData: UserData?
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // UI States
    var isActive by remember { mutableStateOf(false) }
    var radiusKm by remember { mutableFloatStateOf(5f) }

    // Data Lists
    val allActiveAlerts = remember { mutableStateListOf<Alert>() }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    var helperLocation by remember { mutableStateOf<Location?>(null) }

    // Loading state
    var isProcessing by remember { mutableStateOf(false) }

    val helperStats = remember {
        mapOf("responses" to "24", "success" to "91%", "avg_time" to "8m")
    }

    // --- 1. SYNC STATE FROM FIRESTORE ---
    LaunchedEffect(Unit) {
        db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) return@addSnapshotListener
                if (document != null && document.exists()) {
                    isActive = document.getBoolean("isActive") ?: false
                    val dbRadius = document.getDouble("responseRadius")?.toFloat()
                    if (dbRadius != null) radiusKm = dbRadius

                    val locMap = document.get("location") as? Map<String, Double>
                    if (locMap != null) {
                        val loc = Location("provider")
                        loc.latitude = locMap["latitude"] ?: 0.0
                        loc.longitude = locMap["longitude"] ?: 0.0
                        helperLocation = loc
                    }
                }
            }
    }

    // --- 2. AUTO-SAVE RADIUS ---
    LaunchedEffect(radiusKm) {
        delay(1000)
        db.collection("users").document(userId).update("responseRadius", radiusKm)
    }

    // --- 3. TOGGLE ACTIVE STATUS ---
    fun toggleActiveStatus() {
        if (!isActive) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        helperLocation = loc
                        val helperUpdate = mapOf(
                            "isActive" to true,
                            "responseRadius" to radiusKm,
                            "lastActiveTimestamp" to System.currentTimeMillis(),
                            "location" to mapOf("latitude" to loc.latitude, "longitude" to loc.longitude)
                        )
                        db.collection("users").document(userId).update(helperUpdate)
                        Toast.makeText(context, "You are now ACTIVE", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Turn on GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        } else {
            db.collection("users").document(userId).update("isActive", false)
            allActiveAlerts.clear()
            Toast.makeText(context, "You are now OFFLINE", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 4. REAL-TIME ALERT LISTENER ---
    LaunchedEffect(isActive) {
        if (isActive) {
            // Filter: Hide alerts older than 24 hours to remove dummy data
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

            db.collection("alerts")
                .whereEqualTo("status", "active")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshots != null) {
                        allActiveAlerts.clear()
                        for (doc in snapshots) {
                            try {
                                val alert = doc.toObject(Alert::class.java).copy(id = doc.id)

                                // Logic to show all valid alerts within timeframe (including self for testing)
                                if (alert.timestamp > oneDayAgo &&
                                    (alert.location.latitude != 0.0 || alert.location.longitude != 0.0)) {

                                    // Fallback for missing names
                                    if (alert.userName.isBlank()) {
                                        val fixedAlert = alert.copy(userName = "SheShield User")
                                        allActiveAlerts.add(fixedAlert)
                                    } else {
                                        allActiveAlerts.add(alert)
                                    }
                                }
                            } catch (e: Exception) { Log.e("HelperDashboard", "Error", e) }
                        }
                        // Sort by newest first
                        allActiveAlerts.sortByDescending { it.timestamp }
                    }
                }
        }
    }

    // --- 5. ACCEPT ALERT ---
    fun acceptAlert(alert: Alert) {
        isProcessing = true

        // Mark as accepted -> Removes from everyone else's dashboard
        val updates = mapOf(
            "status" to "accepted",
            "responderId" to userId,
            "responderName" to (userData?.name ?: "Helper"),
            "acceptedAt" to System.currentTimeMillis()
        )

        db.collection("alerts").document(alert.id)
            .update(updates)
            .addOnSuccessListener {
                isProcessing = false
                selectedAlert = null

                // Navigate
                com.example.sheshield.screens.helper.HelperTrackingLogic.startNavigationToUser(context, alert)
                onAcceptAlert(alert)
                Toast.makeText(context, "Alert Accepted! Navigating...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isProcessing = false
                Toast.makeText(context, "Failed to accept.", Toast.LENGTH_SHORT).show()
            }
    }

    // --- 6. FILTER ALERTS (RADIUS) ---
    val nearbyAlerts = remember(allActiveAlerts.size, helperLocation, radiusKm) {
        derivedStateOf {
            if (helperLocation == null) emptyList()
            else allActiveAlerts.filter {
                val loc = Location("").apply { latitude = it.location.latitude; longitude = it.location.longitude }
                (helperLocation!!.distanceTo(loc) / 1000) <= radiusKm
            }
        }
    }

    val outsideAlerts = remember(allActiveAlerts.size, helperLocation, radiusKm) {
        derivedStateOf {
            if (helperLocation == null) emptyList()
            else allActiveAlerts.filter {
                val loc = Location("").apply { latitude = it.location.latitude; longitude = it.location.longitude }
                (helperLocation!!.distanceTo(loc) / 1000) > radiusKm
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { toggleActiveStatus() },
                containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                    Icon(if (isActive) Icons.Default.Check else Icons.Default.PowerSettingsNew, "Active")
                    Text(if (isActive) "ACTIVE" else "GO ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1976D2)).padding(24.dp)
                ) {
                    Text("Helper Dashboard", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (isActive) "Scanning for alerts..." else "Go active to help", color = Color.White.copy(alpha = 0.9f))
                }
            }

            if (isActive) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Response Radius: ${radiusKm.roundToInt()} km", fontWeight = FontWeight.Bold)
                            Slider(value = radiusKm, onValueChange = { radiusKm = it }, valueRange = 1f..50f)
                        }
                    }
                }
            }

            // Stats row
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(Icons.Default.AssignmentTurnedIn, helperStats["responses"] ?: "0", "Responses", Color(0xFF6200EE))
                        StatItem(Icons.Default.Star, helperStats["success"] ?: "0%", "Success Rate", Color(0xFF4CAF50))
                        StatItem(Icons.Default.Schedule, helperStats["avg_time"] ?: "0m", "Avg Time", Color(0xFF2196F3))
                    }
                }
            }

            // Quick Actions
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Quick Actions", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QuickActionButton(Icons.Default.Settings, "Settings") { onNavigate(HelperScreen.PROFILE) }
                            QuickActionButton(Icons.Default.Help, "Support") { onNavigate(HelperScreen.SUPPORT) }
                            QuickActionButton(Icons.Default.History, "History") { onNavigate(HelperScreen.HISTORY) }
                            QuickActionButton(Icons.Default.School, "Training") { /* TODO */ }
                        }
                    }
                }
            }

            if (nearbyAlerts.value.isNotEmpty()) {
                item { Text("🚨 URGENT REQUESTS", color = Color.Red, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp)) }
                items(nearbyAlerts.value) { alert ->
                    // Calculate Distance
                    val distStr = if (helperLocation != null) {
                        val tLoc = Location("").apply { latitude = alert.location.latitude; longitude = alert.location.longitude }
                        val d = helperLocation!!.distanceTo(tLoc) / 1000
                        String.format("%.1f km", d)
                    } else "?"

                    NearbyAlertItem(alert, distStr) { selectedAlert = alert }
                }
            } else if (isActive) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No active SOS calls nearby.", color = Color.Gray)
                    }
                }
            }

            if (outsideAlerts.value.isNotEmpty()) {
                item { Text("Outside Radius", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)) }
                items(outsideAlerts.value) { alert ->
                    val distStr = if (helperLocation != null) {
                        val tLoc = Location("").apply { latitude = alert.location.latitude; longitude = alert.location.longitude }
                        val d = helperLocation!!.distanceTo(tLoc) / 1000
                        String.format("%.1f km", d)
                    } else "?"
                    NearbyAlertItem(alert, distStr) { selectedAlert = alert }
                }
            }

            // Switch Mode Button
            item {
                if (onSwitchToUserMode != null) {
                    TextButton(onClick = onSwitchToUserMode, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Icon(Icons.Default.SwitchAccount, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Switch to User Mode")
                    }
                }
            }
        }

        // CONFIRMATION DIALOG
        selectedAlert?.let { alert ->
            AlertDialog(
                onDismissRequest = { if (!isProcessing) selectedAlert = null },
                title = { Text("Respond to ${alert.userName}?") },
                text = {
                    Column {
                        Text("Location: ${alert.location.address}")
                        if(isProcessing) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Accepting request...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { acceptAlert(alert) },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) { Text("RESPOND NOW") }
                },
                dismissButton = {
                    if (!isProcessing) TextButton({ selectedAlert = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = color)
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)) {
            Icon(icon, label, tint = Color(0xFF6200EE))
        }
        Text(label, fontSize = 12.sp)
    }
}

// --- UPDATED AESTHETIC ALERT ITEM ---
@Composable
fun NearbyAlertItem(alert: Alert, distance: String, onAccept: () -> Unit) {
    val timeAgo = remember(alert.timestamp) {
        val diff = System.currentTimeMillis() - alert.timestamp
        val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (mins < 1) "Just now" else "$mins mins ago"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.5.dp, Color(0xFF1976D2)), // Blue Border
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE3F2FD), CircleShape), // Light Blue BG
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alert",
                    tint = Color(0xFFD32F2F), // Red Icon for Urgency
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.userName.ifBlank { "SheShield User" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))

                // Distance Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF1976D2), // Blue Location Pin
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$distance away",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Time Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = timeAgo,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // Accept Button
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F), // Red Button
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("ACCEPT", fontWeight = FontWeight.Bold)
            }
        }
    }
}