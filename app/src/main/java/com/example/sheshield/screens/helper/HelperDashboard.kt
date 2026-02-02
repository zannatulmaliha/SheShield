package com.example.sheshield.screens.helper

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.Alert
import com.example.sheshield.models.UserData

// --- EXACT SHE-SHIELD THEME COLORS ---
private val BackgroundDark = Color(0xFF1A1A2E)
private val SurfaceCard = Color(0xFF25254A)
private val PrimaryPurple = Color(0xFF8B7FFF)
private val AccentEmerald = Color(0xFF34D399)
private val DangerRose = Color(0xFFFB7185)
private val TextPrimary = Color(0xFFE8E8F0)
private val TextSecondary = Color(0xFFB4B4C8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperDashboard(
    onNavigate: (HelperScreen) -> Unit,
    onSwitchToUserMode: (() -> Unit)?,
    onAcceptAlert: (Alert) -> Unit,
    userData: UserData?
) {
    // --- LOGIC PRESERVED ---
    var isActive by remember { mutableStateOf(false) }
    var nearbyAlerts by remember { mutableStateOf<List<Alert>>(emptyList()) }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }

    val mockAlerts = remember {
        listOf(
            Alert(id = "1", userName = "Sarah", alertType = "SOS", riskLevel = "high", description = "SOS triggered - immediate assistance needed", timestamp = System.currentTimeMillis()),
            Alert(id = "2", userName = "Maya", alertType = "check_in", riskLevel = "medium", description = "Missed safety check-in", timestamp = System.currentTimeMillis())
        )
    }

    LaunchedEffect(isActive) {
        nearbyAlerts = if (isActive) mockAlerts else emptyList()
    }

    val helperStats = remember {
        mapOf("responses" to "24", "success" to "91%", "avg_time" to "8m", "trust_score" to "94")
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isActive = !isActive },
                containerColor = if (isActive) AccentEmerald else DangerRose,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 70.dp),
                shape = CircleShape
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (isActive) Icons.Default.Check else Icons.Default.PowerSettingsNew, null)
                    if (userData?.gender == "male") {
                        Text(if (isActive) "ACTIVE" else "OFFLINE", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(BackgroundDark, Color(0xFF16213E)))),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section - Themed with SheShield Gradient
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF4C3F8F), Color(0xFF3A2F6F))))
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Helper Dashboard", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Ready to protect & serve", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }

                        if (userData?.gender == "male") {
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Male, null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                                    Text(" MALE HELPER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            // Status Card - Using SurfaceCard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (isActive) AccentEmerald.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isActive) "SAFETY NETWORK ACTIVE" else "MODE: INACTIVE",
                                color = if (isActive) AccentEmerald else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Toggle to receive nearby SOS alerts", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentEmerald)
                        )
                    }
                }
            }

            // Stats Grid - Using SurfaceCard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Performance Metrics", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(Icons.Default.AssignmentTurnedIn, helperStats["responses"] ?: "0", "Rescues", PrimaryPurple)
                            StatItem(Icons.Default.Star, helperStats["success"] ?: "0%", "Success", AccentEmerald)
                            StatItem(Icons.Default.Schedule, helperStats["avg_time"] ?: "0m", "Avg Time", Color(0xFF2196F3))
                            StatItem(Icons.Default.Security, helperStats["trust_score"] ?: "0", "Trust", Color(0xFFFF9800))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Level Progress Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("LVL ${userData?.helperLevel ?: 1}", color = TextPrimary, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { 0.75f },
                                modifier = Modifier.height(6.dp).weight(1f).padding(horizontal = 12.dp).clip(CircleShape),
                                color = PrimaryPurple,
                                trackColor = BackgroundDark
                            )
                            Text("LVL ${(userData?.helperLevel ?: 1) + 1}", color = TextSecondary)
                        }
                    }
                }
            }

            // Map Placeholder - Themed
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, null, modifier = Modifier.size(40.dp), tint = PrimaryPurple.copy(alpha = 0.5f))
                            Text("Risk Heatmap Active", color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("🔴 High  🟡 Mid  🟢 Safe", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Nearby Alerts Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("NEARBY EMERGENCIES", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        TextButton(onClick = { onNavigate(HelperScreen.ALERTS) }) {
                            Text("View All", color = AccentEmerald, fontSize = 12.sp)
                        }
                    }

                    if (nearbyAlerts.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text(if (!isActive) "Go active to scan for alerts" else "Area is currently secure", color = TextSecondary)
                            }
                        }
                    } else {
                        nearbyAlerts.forEach { alert ->
                            NearbyAlertItem(alert = alert, onAccept = { onAcceptAlert(alert) })
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Male Guidelines - Light Blue variant for info
            if (userData?.gender == "male") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A5F)),
                        border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFF2196F3))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Protocol for Male Responders", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Keep distance and alert authorities first.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        QuickActionButton(Icons.Default.Settings, "Setup", { onNavigate(HelperScreen.PROFILE) })
                        QuickActionButton(Icons.Default.History, "Logs", { onNavigate(HelperScreen.HISTORY) })
                        QuickActionButton(Icons.Default.School, "Academy", {})
                        QuickActionButton(Icons.Default.Help, "Help", { onNavigate(HelperScreen.SUPPORT) })
                    }
                }
            }
        }

        // Dialog - Themed
        selectedAlert?.let { alert ->
            AlertDialog(
                onDismissRequest = { selectedAlert = null },
                containerColor = SurfaceCard,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("Emergency: ${alert.userName}") },
                text = { Text(alert.description) },
                confirmButton = {
                    Button(
                        onClick = { onAcceptAlert(alert); selectedAlert = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) { Text("Respond Now") }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAlert = null }) { Text("Decline", color = DangerRose) }
                }
            )
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun NearbyAlertItem(alert: Alert, onAccept: () -> Unit) {
    val riskColor = when(alert.riskLevel) {
        "high" -> DangerRose
        "medium" -> Color(0xFFFFB74D)
        else -> AccentEmerald
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(alert.userName, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = riskColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(alert.riskLevel.uppercase(), color = riskColor, modifier = Modifier.padding(horizontal = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(alert.description, color = TextSecondary, fontSize = 13.sp, maxLines = 1)
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = riskColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("HELP", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp).background(BackgroundDark, CircleShape)
        ) {
            Icon(icon, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
        }
        Text(label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}