package com.example.sheshield.screens.helper

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Make sure to import this specific modifier for Tabs
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// --- Data Model for Alerts UI ---
data class AlertUIModel(
    val id: String,
    val initial: String,
    val name: String,
    val title: String,
    val riskLevel: String, // "HIGH", "MEDIUM", "LOW"
    val distance: String,
    val timeAgo: String,
    val eta: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperAlertsScreen(
    onBack: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val context = LocalContext.current

    // Colors matched to your screenshots
    val purplePrimary = Color(0xFF9333EA)
    val bgGray = Color(0xFFF9FAFB)

    // Tab State
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Nearby Alerts (6)", "My Response (1)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Alerts", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = purplePrimary)
            )
        },
        // We assume you reuse the HelperBottomBar from Dashboard
        bottomBar = { HelperBottomBar(currentRoute = "Alerts") },
        containerColor = bgGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TABS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = purplePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = purplePrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // --- TAB CONTENT ---
            when (selectedTabIndex) {
                0 -> NearbyAlertsTab(onNavigateToMap)
                1 -> MyResponseTab()
            }
        }
    }
}

// ==========================================
// TAB 1: NEARBY ALERTS LIST
// ==========================================
@Composable
fun NearbyAlertsTab(onViewOnMap: () -> Unit) {
    val purplePrimary = Color(0xFF9333EA)

    // Mock Data based on your screenshots
    val alerts = listOf(
        AlertUIModel("1", "S", "S. Rahman", "Walking home, feeling followed", "HIGH", "0.8km away", "2min ago", "~3min away"),
        AlertUIModel("2", "A", "A. Hossain", "Virtual companion needed for bus ride", "MEDIUM", "1.2km away", "5min ago", "~5min away"),
        AlertUIModel("3", "N", "N. Ahmed", "Car following me for 3 blocks", "HIGH", "1.5km away", "3min ago", "~5min away"),
        AlertUIModel("4", "K", "K. Lima", "Unsafe area at night", "MEDIUM", "3.2km away", "12min ago", "~10min away"),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Showing alerts within 15km", color = Color.Gray, fontSize = 14.sp)
            Text(
                "Change radius",
                color = purplePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* Open Dialog */ }
            )
        }

        // List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(alerts) { alert ->
                AlertListCard(alert, onViewOnMap)
            }
        }
    }
}

@Composable
fun AlertListCard(alert: AlertUIModel, onViewOnMap: () -> Unit) {
    val purplePrimary = Color(0xFF9333EA)

    // Risk Colors
    val (bgColor, textColor, label) = when(alert.riskLevel) {
        "HIGH" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "HIGH RISK")
        "MEDIUM" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "MEDIUM RISK")
        else -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "LOW RISK")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(verticalAlignment = Alignment.Top) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF3E8FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(alert.initial, color = purplePrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    // Risk Tag & Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = bgColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${alert.distance} • ${alert.timeAgo}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(alert.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Text(" ${alert.name}   ", fontSize = 12.sp, color = Color.Gray)
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Text(" ${alert.eta}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = purplePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ACCEPT ALERT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = onViewOnMap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("View on Map", color = Color(0xFF1F2937), fontSize = 12.sp)
                }
            }
        }
    }
}

// ==========================================
// TAB 2: MY RESPONSE (ACTIVE)
// ==========================================
@Composable
fun MyResponseTab() {
    val scrollState = rememberScrollState()
    val purplePrimary = Color(0xFF9333EA)

    // Status Tracker State (1=En Route, 2=Arrived, 3=Assisting)
    var responseStatus by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- ACTIVE RESPONSE CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // User Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(purplePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Helping R. Khan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(4.dp)) {
                                Text("En Route", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                        Text("Started 3 min ago", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Info Stats
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text(" ETA", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("3 min", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Text(" Distance", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("1.2 km", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Location", fontSize = 12.sp, color = Color.Gray)
                Text("Gulshan Avenue, Dhaka", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResponseActionButton(Modifier.weight(1f), "Navigate", Icons.Default.NearMe, Color(0xFF2563EB))
                    ResponseActionButton(Modifier.weight(1f), "Call 999", Icons.Default.Call, Color(0xFF16A34A))
                    ResponseActionButton(Modifier.weight(1f), "Chat", Icons.AutoMirrored.Filled.Chat, purplePrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SAFETY REMINDERS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light Orange
            border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Safety Reminders", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                }
                Spacer(modifier = Modifier.height(8.dp))
                SafetyBullet("Maintain communication with the person in need")
                SafetyBullet("Do not put yourself in danger")
                SafetyBullet("Call emergency services (999) if situation escalates")
                SafetyBullet("Stay in well-lit, public areas when possible")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- UPDATE STATUS ---
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Update Response Status", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                StatusOption("En Route to Location", Icons.Default.NearMe, responseStatus == 1) { responseStatus = 1 }
                Spacer(modifier = Modifier.height(8.dp))
                StatusOption("Arrived at Location", Icons.Default.LocationOn, responseStatus == 2) { responseStatus = 2 }
                Spacer(modifier = Modifier.height(8.dp))
                StatusOption("Currently Assisting", Icons.Default.Shield, responseStatus == 3) { responseStatus = 3 }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resolution Buttons
        Button(onClick = { /* Finish */ }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)), shape = RoundedCornerShape(8.dp)) {
            Text("Mark as Resolved", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { /* Cancel */ }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)), border = BorderStroke(1.dp, Color(0xFFFECACA)), shape = RoundedCornerShape(8.dp)) {
            Text("Cancel Response", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun ResponseActionButton(modifier: Modifier, text: String, icon: ImageVector, color: Color) {
    Button(
        onClick = {},
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SafetyBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = Color(0xFF9A3412), modifier = Modifier.padding(end = 6.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF9A3412), lineHeight = 18.sp)
    }
}

@Composable
fun StatusOption(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF0F172A) else Color.White // Dark Navy vs White
    val contentColor = if (isSelected) Color.White else Color(0xFF1F2937)
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB)

    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, color = contentColor)
        }
    }
}

// Re-using the Bottom Bar for consistency
@Composable
fun HelperBottomBar(currentRoute: String) {
    val purplePrimary = Color(0xFF9333EA)
    val context = LocalContext.current

    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("Dashboard", Icons.Default.Shield, false),
            Triple("Alerts", Icons.Default.Notifications, true), // Active
            Triple("Profile", Icons.Default.Person, false),
            Triple("Help", Icons.AutoMirrored.Filled.Help, false)
        )

        items.forEach { (label, icon, isSelected) ->
            NavigationBarItem(
                icon = { Icon(icon, null) },
                label = { Text(label) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = purplePrimary,
                    selectedTextColor = purplePrimary,
                    indicatorColor = Color(0xFFF3E8FF)
                ),
                onClick = {
                    if (!isSelected) Toast.makeText(context, "Navigating to $label", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}