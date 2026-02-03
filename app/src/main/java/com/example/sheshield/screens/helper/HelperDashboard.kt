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
    var isProcessing by remember { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    var helperLocation by remember { mutableStateOf<Location?>(null) }

    // Data List (Single Source of Truth)
    val allActiveAlerts = remember { mutableStateListOf<Alert>() }

    val helperStats = remember {
        mapOf("responses" to "24", "success" to "91%", "avg_time" to "8m")
    }

    // --- 1. SYNC INITIAL STATE ---
    LaunchedEffect(Unit) {
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                isActive = doc.getBoolean("isActive") ?: false
                radiusKm = doc.getDouble("responseRadius")?.toFloat() ?: 5f
            }
        }
    }

    // --- 2. LOCATION PERMISSION & UPDATES ---
    LaunchedEffect(isActive) {
        if (isActive) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    helperLocation = loc
                }
            }
        }
    }

    // --- 3. AUTO-SAVE RADIUS ---
    LaunchedEffect(radiusKm) {
        delay(1000)
        db.collection("users").document(userId).update("responseRadius", radiusKm)
    }

    // --- 4. REAL-TIME ALERT LISTENER ---
    LaunchedEffect(isActive) {
        if (isActive) {
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val listener = db.collection("alerts")
                .whereEqualTo("status", "active")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshots != null) {
                        allActiveAlerts.clear()
                        for (doc in snapshots) {
                            try {
                                val alert = doc.toObject(Alert::class.java).copy(id = doc.id)
                                if (alert.timestamp > oneDayAgo && alert.userId != userId) {
                                    allActiveAlerts.add(alert)
                                }
                            } catch (e: Exception) { Log.e("Dashboard", "Error parsing", e) }
                        }
                        allActiveAlerts.sortByDescending { it.timestamp }
                    }
                }
            // Clean up listener when effect leaves or isActive changes
        } else {
            allActiveAlerts.clear()
        }
    }

    // --- 5. DERIVED FILTERED LISTS (FIXES THE "CLEAR" ERROR) ---
    // These update automatically when any dependent state changes.
    val nearbyAlerts by remember(allActiveAlerts, helperLocation, radiusKm) {
        derivedStateOf {
            if (helperLocation == null) emptyList<Alert>()
            else allActiveAlerts.filter {
                val alertLoc = Location("").apply {
                    latitude = it.location.latitude; longitude = it.location.longitude
                }
                (helperLocation!!.distanceTo(alertLoc) / 1000) <= radiusKm
            }
        }
    }

    val outsideAlerts by remember(allActiveAlerts, helperLocation, radiusKm) {
        derivedStateOf {
            if (helperLocation == null) emptyList<Alert>()
            else allActiveAlerts.filter {
                val alertLoc = Location("").apply {
                    latitude = it.location.latitude; longitude = it.location.longitude
                }
                (helperLocation!!.distanceTo(alertLoc) / 1000) > radiusKm
            }
        }
    }

    // --- 6. ACTIONS ---
    fun toggleActiveStatus() {
        val newState = !isActive
        db.collection("users").document(userId).update("isActive", newState)
        isActive = newState
        val msg = if (newState) "You are now ACTIVE" else "You are now OFFLINE"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun acceptAlert(alert: Alert) {
        isProcessing = true
        val updates = mapOf(
            "status" to "accepted",
            "responderId" to userId,
            "responderName" to (userData?.name ?: "Helper"),
            "acceptedAt" to System.currentTimeMillis()
        )
        db.collection("alerts").document(alert.id).update(updates)
            .addOnSuccessListener {
                isProcessing = false
                selectedAlert = null
                onAcceptAlert(alert)
            }
            .addOnFailureListener {
                isProcessing = false
                Toast.makeText(context, "Failed to accept.", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { toggleActiveStatus() },
                containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                    Icon(if (isActive) Icons.Default.Check else Icons.Default.PowerSettingsNew, null)
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
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1976D2)).padding(24.dp)) {
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

                // Stats row
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(Icons.Default.AssignmentTurnedIn, helperStats["responses"] ?: "0", "Responses", Color(0xFF6200EE))
                            StatItem(Icons.Default.Star, helperStats["success"] ?: "0%", "Success", Color(0xFF4CAF50))
                            StatItem(Icons.Default.Schedule, helperStats["avg_time"] ?: "0m", "Avg Time", Color(0xFF2196F3))
                        }
                    }
                }

                if (nearbyAlerts.isNotEmpty()) {
                    item { Text("🚨 URGENT REQUESTS", color = Color.Red, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp)) }
                    items(nearbyAlerts) { alert ->
                        val distStr = helperLocation?.let {
                            val loc = Location("").apply { latitude = alert.location.latitude; longitude = alert.location.longitude }
                            String.format("%.1f km", it.distanceTo(loc) / 1000)
                        } ?: "?"
                        NearbyAlertItem(alert, distStr) { selectedAlert = alert }
                    }
                } else {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No active SOS calls nearby.", color = Color.Gray)
                        }
                    }
                }

                if (outsideAlerts.isNotEmpty()) {
                    item { Text("Outside Radius", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)) }
                    items(outsideAlerts) { alert ->
                        val distStr = helperLocation?.let {
                            val loc = Location("").apply { latitude = alert.location.latitude; longitude = alert.location.longitude }
                            String.format("%.1f km", it.distanceTo(loc) / 1000)
                        } ?: "?"
                        NearbyAlertItem(alert, distStr) { selectedAlert = alert }
                    }
                }
            }

            // Quick Actions & Switch Mode
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        QuickActionButton(Icons.Default.Settings, "Profile") { onNavigate(HelperScreen.PROFILE) }
                        QuickActionButton(Icons.Default.Help, "Support") { onNavigate(HelperScreen.SUPPORT) }
                        QuickActionButton(Icons.Default.History, "History") { onNavigate(HelperScreen.HISTORY) }
                    }
                    if (onSwitchToUserMode != null) {
                        Spacer(Modifier.height(24.dp))
                        TextButton(onClick = onSwitchToUserMode, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.SwitchAccount, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Switch to User Mode")
                        }
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
        Icon(icon, null, tint = color)
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)) {
            Icon(icon, null, tint = Color(0xFF6200EE))
        }
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun NearbyAlertItem(alert: Alert, distance: String, onAccept: () -> Unit) {
    val timeAgo = remember(alert.timestamp) {
        val diff = System.currentTimeMillis() - alert.timestamp
        val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (mins < 1) "Just now" else "$mins mins ago"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, Color(0xFF1976D2)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFFD32F2F))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.userName.ifBlank { "SheShield User" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$distance away • $timeAgo", fontSize = 13.sp, color = Color.Gray)
            }
            Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                Text("VIEW")
            }
        }
    }
}