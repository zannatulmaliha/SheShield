package com.example.sheshield.screens.helper

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.UserData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HelperProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    userData: UserData?
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()

    // Colors
    val purplePrimary = Color(0xFF9333EA)
    val purpleLight = Color(0xFFF3E8FF)
    val bgGray = Color(0xFFF9FAFB)
    val textDark = Color(0xFF1F2937)
    val textGray = Color(0xFF6B7280)
    val successGreen = Color(0xFF22C55E)
    val warningOrange = Color(0xFFF59E0B)

    // --- State for Editable Settings ---
    var responseRadius by remember { mutableFloatStateOf(userData?.responseRadius?.toFloat() ?: 15f) }
    var activeHoursStart by remember { mutableStateOf("08:00") }
    var activeHoursEnd by remember { mutableStateOf("22:00") }
    var is24Hours by remember { mutableStateOf(true) }

    // Alert Preferences State
    val alertOptions = listOf("Low Risk", "Medium Risk", "High Risk")
    val selectedAlerts = remember { mutableStateListOf("Low Risk", "Medium Risk", "High Risk") }

    // Dialog Visibility States
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }
    var showHoursDialog by remember { mutableStateOf(false) }

    // Stats State
    val helperLevel by remember { mutableIntStateOf(3) }
    val progressToNextLevel by remember { mutableFloatStateOf(0.85f) }
    val totalResponses by remember { mutableIntStateOf(47) }

    // Format "Member Since" Date
    // Assuming userData.createdAt is a Long timestamp or String date.
    // Adapting to display actual data or fallback.
    val memberSinceText = remember(userData) {
        val dateStr = userData?.createdAt ?: "March 2024"
        // Add actual date formatting logic here if needed
        dateStr
    }

    Scaffold(
        containerColor = bgGray,
        bottomBar = { HelperBottomBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(scrollState)
        ) {
            // --- HEADER SECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Purple Background Curve
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(purplePrimary)
                )

                // Top Bar
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Text(
                            "Helper Profile",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // --- PROFILE CARD ---
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD8B4FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userData?.name?.take(2)?.uppercase() ?: "JC",
                                fontSize = 28.sp,
                                color = purplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            userData?.name ?: "Jane Cooper",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                        Text(
                            text = "Helper ID: #HLP-${auth.currentUser?.uid?.take(4) ?: "4892"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = warningOrange, modifier = Modifier.size(20.dp))
                            Text(" 4.8/5 ", fontWeight = FontWeight.Bold, color = textDark)
                            Text("($totalResponses responses)", color = textGray, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            SkillChip("First Aid")
                            Spacer(Modifier.width(8.dp))
                            SkillChip("CPR Certified")
                            Spacer(Modifier.width(8.dp))
                            SkillChip("Safety Training")
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {

                // --- VERIFICATION STATUS ---
                SectionHeader("Verification Status")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = borderIf(userData?.isHelperVerified == true, successGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFFDCFCE7), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, null, tint = successGreen)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Fully Verified", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                        Text(" Active ", color = successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text("All verification documents approved", fontSize = 12.sp, color = textGray)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Docs Verified", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = purplePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, purpleLight)
                        ) {
                            Text("View Verification Documents")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- HELPER LEVEL ---
                SectionHeader("Helper Level")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(purpleLight, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MilitaryTech, null, tint = purplePrimary)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Level $helperLevel", fontWeight = FontWeight.Bold)
                                    Text("Experienced Helper", fontSize = 12.sp, color = textGray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${(progressToNextLevel * 100).toInt()}/100", fontWeight = FontWeight.Bold, color = purplePrimary)
                                Text("to Level ${helperLevel + 1}", fontSize = 10.sp, color = textGray)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progressToNextLevel },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = purplePrimary,
                            trackColor = Color(0xFFE5E7EB),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Complete 15 more successful responses to reach Level 4", fontSize = 12.sp, color = textGray)
                        Spacer(Modifier.height(16.dp))
                        Surface(color = purpleLight.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Level 3 Benefits:", fontWeight = FontWeight.SemiBold, color = purplePrimary, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                BenefitItem("Access to all alert types")
                                BenefitItem("Priority notification")
                                BenefitItem("Extended response radius (up to 20km)")
                                BenefitItem("Advanced safety tools")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- RESPONSE STATISTICS ---
                SectionHeader("Response Statistics")
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            StatBox(Modifier.weight(1f), "$totalResponses", "Total Responses", purpleLight, purplePrimary)
                            Spacer(Modifier.width(12.dp))
                            StatBox(Modifier.weight(1f), "45", "Successful", Color(0xFFDCFCE7), successGreen)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            StatBox(Modifier.weight(1f), "4.2min", "Avg Response Time", Color(0xFFE0F2FE), Color(0xFF0284C7))
                            Spacer(Modifier.width(12.dp))
                            StatBox(Modifier.weight(1f), "96%", "Success Rate", Color(0xFFFFEDD5), warningOrange)
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Full Response History", color = textDark)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, null, tint = textGray)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- EMERGENCY CONTACT ---
                SectionHeader("Emergency Contact")
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp).background(Color(0xFFFEE2E2), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Call, null, tint = Color(0xFFDC2626))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mom", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("(880) 1812345678", color = textGray, fontSize = 14.sp)
                        }
                        TextButton(onClick = { Toast.makeText(context, "Edit Contact", Toast.LENGTH_SHORT).show() }) {
                            Text("Edit", color = purplePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- SETTINGS (EDITABLE) ---
                SectionHeader("Settings")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // 1. Response Radius
                        SettingItem(
                            icon = Icons.Default.LocationOn,
                            title = "Response Radius",
                            subtitle = "Currently: ${responseRadius.toInt()}km",
                            onClick = { showRadiusDialog = true }
                        )
                        HorizontalDivider(color = bgGray)

                        // 2. Alert Preferences
                        SettingItem(
                            icon = Icons.Default.Notifications,
                            title = "Alert Preferences",
                            subtitle = if (selectedAlerts.size == 3) "All Risks" else selectedAlerts.joinToString(", "),
                            onClick = { showAlertsDialog = true }
                        )
                        HorizontalDivider(color = bgGray)

                        // 3. Active Hours
                        SettingItem(
                            icon = Icons.Default.AccessTime,
                            title = "Active Hours",
                            subtitle = if (is24Hours) "24/7 availability" else "$activeHoursStart - $activeHoursEnd",
                            onClick = { showHoursDialog = true }
                        )
                        HorizontalDivider(color = bgGray)

                        // 4. Guidelines (Not Editable, just navigation)
                        SettingItem(
                            icon = Icons.Default.Shield,
                            title = "Helper Guidelines",
                            subtitle = "Safety protocols & best practices",
                            onClick = { Toast.makeText(context, "Opening Guidelines PDF...", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- FOOTER (DYNAMIC DATA) ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = purpleLight.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // DYNAMIC MEMBER SINCE
                            Text(
                                text = "Member since $memberSinceText",
                                color = purplePrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Thank you for being part of the SheShield helper community!",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = purplePrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", color = Color.Red)
                    }
                }
            }
        }

        // --- DIALOGS ---

        // Radius Edit Dialog
        if (showRadiusDialog) {
            AlertDialog(
                onDismissRequest = { showRadiusDialog = false },
                title = { Text("Response Radius") },
                text = {
                    Column {
                        Text("Set the maximum distance you are willing to travel for help.")
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "${responseRadius.toInt()} km",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Slider(
                            value = responseRadius,
                            onValueChange = { responseRadius = it },
                            valueRange = 1f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(thumbColor = purplePrimary, activeTrackColor = purplePrimary)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Update Logic Here (Firebase update)
                        showRadiusDialog = false
                    }) { Text("Save", color = purplePrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showRadiusDialog = false }) { Text("Cancel", color = textGray) }
                }
            )
        }

        // Alert Preferences Dialog
        if (showAlertsDialog) {
            AlertDialog(
                onDismissRequest = { showAlertsDialog = false },
                title = { Text("Alert Preferences") },
                text = {
                    Column {
                        alertOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedAlerts.contains(option)) {
                                            if (selectedAlerts.size > 1) selectedAlerts.remove(option) // Prevent empty
                                        } else {
                                            selectedAlerts.add(option)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedAlerts.contains(option),
                                    onCheckedChange = null, // Handled by Row click
                                    colors = CheckboxDefaults.colors(checkedColor = purplePrimary)
                                )
                                Text(text = option, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAlertsDialog = false }) { Text("Save", color = purplePrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAlertsDialog = false }) { Text("Cancel", color = textGray) }
                }
            )
        }

        // Active Hours Dialog
        if (showHoursDialog) {
            AlertDialog(
                onDismissRequest = { showHoursDialog = false },
                title = { Text("Active Hours") },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { is24Hours = !is24Hours }
                        ) {
                            Switch(
                                checked = is24Hours,
                                onCheckedChange = { is24Hours = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = purplePrimary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("24/7 Availability")
                        }

                        if (!is24Hours) {
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = activeHoursStart,
                                onValueChange = { activeHoursStart = it },
                                label = { Text("Start Time") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = activeHoursEnd,
                                onValueChange = { activeHoursEnd = it },
                                label = { Text("End Time") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHoursDialog = false }) { Text("Save", color = purplePrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showHoursDialog = false }) { Text("Cancel", color = textGray) }
                }
            )
        }
    }
}

// --- SUB-COMPONENTS ---
@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF9333EA), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF9CA3AF))
    }
}

// ... (Other existing sub-components: SkillChip, BenefitItem, StatBox, SectionHeader, borderIf, HelperBottomBar remain unchanged)
// You should include the unchanged sub-components from the previous functional code here to make it complete.
@Composable
fun SkillChip(text: String) {
    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(16.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B5563))
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, null, tint = Color(0xFF9333EA), modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, value: String, label: String, color: Color, textColor: Color) {
    Column(modifier = modifier.background(color, RoundedCornerShape(12.dp)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColor)
        Text(label, fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp), style = MaterialTheme.typography.titleMedium, color = Color(0xFF374151))
}

@Composable
fun borderIf(condition: Boolean, color: Color): androidx.compose.foundation.BorderStroke? {
    return if (condition) androidx.compose.foundation.BorderStroke(1.dp, color) else null
}

@Composable
fun HelperBottomBar() {
    val context = LocalContext.current
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(icon = { Icon(Icons.Default.Shield, null) }, label = { Text("Dashboard") }, selected = false, onClick = { })
        NavigationBarItem(icon = { Icon(Icons.Default.Notifications, null) }, label = { Text("Alerts") }, selected = false, onClick = { })
        NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") }, selected = true, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF9333EA), selectedTextColor = Color(0xFF9333EA), indicatorColor = Color(0xFFF3E8FF)), onClick = { })
        NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Filled.Help, null) }, label = { Text("Help") }, selected = false, onClick = { })
    }
}