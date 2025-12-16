package com.example.sheshield.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    // Mock stats data
    val helperStats = remember {
        mapOf(
            "responses" to "24",
            "success" to "91%",
            "avg_time" to "8m",
            "trust_score" to "94"
        )
    }

    // Mock alerts for demonstration
    LaunchedEffect(key1 = isActive) {
        if (isActive) {
            nearbyAlerts = listOf(
                Alert(
                    id = "1",
                    userName = "Sarah",
                    alertType = "SOS",
                    riskLevel = "high",
                    description = "SOS triggered - immediate assistance needed",
                    timestamp = System.currentTimeMillis() - 300000
                ),
                Alert(
                    id = "2",
                    userName = "Maya",
                    alertType = "check_in",
                    riskLevel = "medium",
                    description = "Missed safety check-in",
                    timestamp = System.currentTimeMillis() - 900000
                )
            )
        } else {
            nearbyAlerts = emptyList()
        }
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

                        // Gender badge
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
                        Column(
                            modifier = Modifier
                        ) {
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

            // Quick Stats Grid
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Your Stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                icon = Icons.Default.AssignmentTurnedIn,
                                value = helperStats["responses"] ?: "0",
                                label = "Responses",
                                color = Color(0xFF6200EE)
                            )
                            StatItem(
                                icon = Icons.Default.Star,
                                value = helperStats["success"] ?: "0%",
                                label = "Success Rate",
                                color = Color(0xFF4CAF50)
                            )
                            StatItem(
                                icon = Icons.Default.Schedule,
                                value = helperStats["avg_time"] ?: "0m",
                                label = "Avg Time",
                                color = Color(0xFF2196F3)
                            )
                            StatItem(
                                icon = Icons.Default.Security,
                                value = helperStats["trust_score"] ?: "0",
                                label = "Trust Score",
                                color = Color(0xFFFF9800)
                            )
                        }

                        // Helper Level Progress
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Level ${userData?.helperLevel ?: 1}",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            LinearProgressIndicator(
                                progress = { 0.75f },
                                modifier = Modifier
                                    .height(8.dp)
                                    .fillMaxWidth(),
                                color = Color(0xFF6200EE),
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Level ${(userData?.helperLevel ?: 1) + 1}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "75% to next level",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Interactive Map Placeholder
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "Map",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Interactive Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Color-coded alert pins:\n🔴 High 🟡 Medium 🟢 Low",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            // Mock Alert Pins
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AlertPin(
                                    color = Color.Red,
                                    onClick = {
                                        selectedAlert = Alert(
                                            id = "1",
                                            userName = "High Risk Alert",
                                            alertType = "SOS",
                                            riskLevel = "high",
                                            description = "Mock high risk alert for demo"
                                        )
                                    }
                                )
                                AlertPin(
                                    color = Color.Yellow,
                                    onClick = {
                                        selectedAlert = Alert(
                                            id = "2",
                                            userName = "Medium Risk Alert",
                                            alertType = "Check-in",
                                            riskLevel = "medium",
                                            description = "Mock medium risk alert for demo"
                                        )
                                    }
                                )
                                AlertPin(
                                    color = Color.Green,
                                    onClick = {
                                        selectedAlert = Alert(
                                            id = "3",
                                            userName = "Low Risk Alert",
                                            alertType = "Safety",
                                            riskLevel = "low",
                                            description = "Mock low risk alert for demo"
                                        )
                                    }
                                )
                            }
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nearby Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { onNavigate(HelperScreen.ALERTS) }) {
                                Text("See All")
                            }
                        }

                        if (nearbyAlerts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsOff,
                                        "No Alerts",
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No alerts nearby",
                                        color = Color.Gray
                                    )
                                    if (!isActive) {
                                        Text(
                                            "Go active to receive alerts",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
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

            // Gender-specific Guidelines (for male helpers)
            if (userData?.gender == "male") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF1976D2)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    "Guidelines",
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Male Helper Guidelines",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "• Announce yourself clearly when approaching\n" +
                                        "• Maintain respectful distance at all times\n" +
                                        "• Call emergency services first if needed\n" +
                                        "• Do not enter private residences\n" +
                                        "• Keep location sharing enabled",
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Quick Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Settings,
                                label = "Settings",
                                onClick = { onNavigate(HelperScreen.PROFILE) }
                            )
                            QuickActionButton(
                                icon = Icons.Default.Help,
                                label = "Support",
                                onClick = { onNavigate(HelperScreen.SUPPORT) }
                            )
                            QuickActionButton(
                                icon = Icons.Default.History,
                                label = "History",
                                onClick = { /* Open history */ }
                            )
                            QuickActionButton(
                                icon = Icons.Default.School,
                                label = "Training",
                                onClick = { /* Open training */ }
                            )
                        }
                    }
                }
            }
        }

        // Alert Detail Dialog
        selectedAlert?.let { alert ->
            AlertDialog(
                onDismissRequest = { selectedAlert = null },
                title = {
                    Text("Alert Details - ${alert.userName}")
                },
                text = {
                    Column {
                        // Risk level badge
                        Box(
                            modifier = Modifier
                                .background(
                                    when(alert.riskLevel) {
                                        "high" -> Color.Red
                                        "medium" -> Color.Yellow
                                        else -> Color.Green
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                alert.riskLevel.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Alert Type: ${alert.alertType.uppercase()}",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            alert.description,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            "Distance: 2.3 km away",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAcceptAlert(alert)
                            selectedAlert = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, "Accept")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Accept Alert")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedAlert = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlertPin(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            "Alert Pin",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun NearbyAlertItem(alert: Alert, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(alert.riskLevel) {
                "high" -> Color(0xFFFFEBEE)
                "medium" -> Color(0xFFFFF8E1)
                else -> Color(0xFFE8F5E9)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when(alert.riskLevel) {
                "high" -> Color.Red
                "medium" -> Color.Yellow
                else -> Color.Green
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alert.userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when(alert.riskLevel) {
                                    "high" -> Color.Red
                                    "medium" -> Color.Yellow
                                    else -> Color.Green
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            alert.riskLevel.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    alert.description,
                    fontSize = 14.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    "2.3 km away • 5 min ago",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onAccept,
                modifier = Modifier.padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("Accept")
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                label,
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Helper Screen Enum
enum class HelperScreen {
    DASHBOARD, ALERTS, PROFILE, SUPPORT
}