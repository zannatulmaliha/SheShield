package com.example.sheshield.screens.helper

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.Alert
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.helper.HelperSupportScreen
// Kept tracking logic so the map feature still works
//import com.example.sheshield.screens.helper.HelperTrackingLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperDashboard(
    onNavigate: (HelperScreen) -> Unit,
    onSwitchToUserMode: (() -> Unit)?,
    onAcceptAlert: (Alert) -> Unit,
    userData: UserData?
) {
    var isActive by remember { mutableStateOf(false) }
    var nearbyAlerts by remember { mutableStateOf<List<Alert>>(emptyList()) }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }

    // 1. MOCK DATA RESTORED
    // These alerts will appear when you flip the switch to "Active"
    val mockAlerts = remember {
        listOf(
            Alert(
                id = "1",
                userName = "Sarah",
                alertType = "SOS",
                riskLevel = "high",
                description = "SOS triggered - immediate assistance needed",
                timestamp = System.currentTimeMillis()
            ),
            Alert(
                id = "2",
                userName = "Maya",
                alertType = "check_in",
                riskLevel = "medium",
                description = "Missed safety check-in",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // 2. Logic to toggle alerts based on Active status
    LaunchedEffect(isActive) {
        if (isActive) {
            nearbyAlerts = mockAlerts
        } else {
            nearbyAlerts = emptyList()
        }
    }

    // Mock stats data
    val helperStats = remember {
        mapOf(
            "responses" to "24",
            "success" to "91%",
            "avg_time" to "8m",
            "trust_score" to "94"
        )
    }

    Scaffold(
        floatingActionButton = {
            if (isActive) {
                FloatingActionButton(
                    onClick = { isActive = !isActive },
                    containerColor = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 70.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Check, "Active")
                        if (userData?.gender == "male") {
                            Text(
                                "ACTIVE",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = { isActive = !isActive },
                    containerColor = Color(0xFFF44336),
                    modifier = Modifier.padding(bottom = 70.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, "Inactive")
                        if (userData?.gender == "male") {
                            Text(
                                "INACTIVE",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1976D2))
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Helper Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ready to help others",
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        if (userData?.gender == "male") {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Male,
                                        "Male Helper",
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "MALE HELPER",
                                        color = Color(0xFF1976D2),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (isActive) "ACTIVE - Receiving Alerts" else "INACTIVE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                if (isActive) "You will receive nearby emergency alerts"
                                else "Toggle to start receiving alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF9E9E9E)
                            )
                        )
                    }
                }
            }

            // Stats Grid
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(Icons.Default.AssignmentTurnedIn, helperStats["responses"] ?: "0", "Responses", Color(0xFF6200EE))
                            StatItem(Icons.Default.Star, helperStats["success"] ?: "0%", "Success Rate", Color(0xFF4CAF50))
                            StatItem(Icons.Default.Schedule, helperStats["avg_time"] ?: "0m", "Avg Time", Color(0xFF2196F3))
                            StatItem(Icons.Default.Security, helperStats["trust_score"] ?: "0", "Trust Score", Color(0xFFFF9800))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Level ${userData?.helperLevel ?: 1}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            LinearProgressIndicator(
                                progress = { 0.75f },
                                modifier = Modifier.height(8.dp).weight(1f),
                                color = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Level ${(userData?.helperLevel ?: 1) + 1}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive Map Placeholder
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, "Map", modifier = Modifier.size(48.dp), tint = Color(0xFF6200EE))
                            Text("Interactive Map", fontWeight = FontWeight.Bold)
                            Text("🔴 High 🟡 Medium 🟢 Low", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Nearby Alerts Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nearby Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { onNavigate(HelperScreen.ALERTS) }) {
                                Text("See All")
                            }
                        }

                        // Use isActive to show/hide content
                        if (nearbyAlerts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.NotificationsOff, "No Alerts", tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(if (!isActive) "Go active to receive alerts" else "No alerts nearby", color = Color.Gray)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                nearbyAlerts.forEach { alert ->
                                    NearbyAlertItem(
                                        alert = alert,
                                        onAccept = { onAcceptAlert(alert) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Male Guidelines
            if (userData?.gender == "male") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Male Helper Guidelines", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                            Text("• Announce yourself clearly\n• Maintain respectful distance\n• Call emergency services if needed", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Quick Actions", fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QuickActionButton(Icons.Default.Settings, "Settings", { onNavigate(HelperScreen.PROFILE) })
                            QuickActionButton(Icons.Default.Help, "Support", { onNavigate(HelperScreen.SUPPORT) })
                            QuickActionButton(Icons.Default.History, "History", { onNavigate(HelperScreen.HISTORY) })
                            QuickActionButton(Icons.Default.School, "Training", { /* Training */ })
                        }
                    }
                }
            }
        }

        // Alert Detail Dialog
        selectedAlert?.let { alert ->
            val context = androidx.compose.ui.platform.LocalContext.current
            AlertDialog(
                onDismissRequest = { selectedAlert = null },
                title = { Text("Alert Details - ${alert.userName}") },
                text = { Text(alert.description) },
                confirmButton = {
                    Button(
                        onClick = {
                            //HelperTrackingLogic.startNavigationToUser(context, alert)
                            onAcceptAlert(alert)
                            selectedAlert = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept Alert")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAlert = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        }
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun AlertPin(color: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Default.LocationOn, "Pin", tint = color, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun NearbyAlertItem(alert: Alert, onAccept: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(alert.riskLevel) {
                "high" -> Color(0xFFFFEBEE)
                "medium" -> Color(0xFFFFF8E1)
                else -> Color(0xFFE8F5E9)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, when(alert.riskLevel) {
            "high" -> Color.Red
            "medium" -> Color.Yellow
            else -> Color.Green
        })
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(alert.userName, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(when(alert.riskLevel) {
                        "high" -> Color.Red
                        "medium" -> Color.Yellow
                        else -> Color.Green
                    }, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(alert.riskLevel.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(alert.description, fontSize = 14.sp, maxLines = 2)
                Text("Real-time Alert • GPS active", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = {
                    // Call your tracking logic here!
                    //HelperTrackingLogic.startNavigationToUser(context, alert)
                    onAccept()
                },
                modifier = Modifier.padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Accept")
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(56.dp).background(Color(0xFFF5F5F5), CircleShape).padding(12.dp)) {
            IconButton(onClick = onClick) {
                Icon(icon, label, tint = Color(0xFF6200EE))
            }
        }
        Text(label, fontSize = 12.sp)
    }
}
