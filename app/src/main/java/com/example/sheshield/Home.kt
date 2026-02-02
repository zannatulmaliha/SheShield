package com.example.sheshield

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.SOS.*
import com.example.sheshield.SOS.SosViewModel
import com.example.sheshield.screens.*
import com.example.sheshield.ui.screens.TimedCheckIn
import com.example.sheshield.viewmodel.MovementViewModel
import com.example.sheshield.services.VoiceCommandService
import com.example.sheshield.screens.TrackRouteScreen

@Composable
fun HomeScreen(
    movementViewModel: MovementViewModel,
    sosViewModel: SosViewModel = viewModel(),
    onCardOneClick: () -> Unit, // Kept for compatibility
    onCardTwoClick: () -> Unit,
    onCardFiveClick: () -> Unit,
    onMovementScreenClick: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("home") }
    var showMovementScreen by remember { mutableStateOf(false) }

    // Collect movement state
    val movementState by movementViewModel.movementState.collectAsState()

    // MASTER NAVIGATION SWITCH
    when (currentScreen) {
        "home" -> HomeContent(
            sosViewModel = sosViewModel,
            movementViewModel = movementViewModel,
            // --- LOCAL NAVIGATION LOGIC ---
            onCardOneClick = { currentScreen = "trackRoute" },
            onCardTwoClick = { currentScreen = "timedCheckIn" },
            onCardFiveClick = { currentScreen = "responders" },
            // ------------------------------
            onMovementScreenClick = { showMovementScreen = true }
        )
        "timedCheckIn" -> TimedCheckIn(
            onNavigate = { currentScreen = it }, // Handles "home" or other destinations
            onBack = { currentScreen = "home" }
        )
        "responders" -> RespondersNearMeScreen(
            onBackClick = { currentScreen = "home" }
        )
        "trackRoute" -> TrackRouteScreen(
            onBack = { currentScreen = "home" }
        )
    }

    // Movement Detection Screen Overlay
    if (showMovementScreen) {
        MovementDetectionScreen(
            onBack = { showMovementScreen = false },
            onAbnormalMovementDetected = { type, confidence ->
                println("🚨 Abnormal movement detected: $type ($confidence)")
            }
        )
    }
}

@Composable
fun HomeContent(
    sosViewModel: SosViewModel,
    movementViewModel: MovementViewModel,
    onCardOneClick: () -> Unit,
    onCardTwoClick: () -> Unit,
    onCardFiveClick: () -> Unit,
    onMovementScreenClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val sosState by sosViewModel.sosState.collectAsState()
    val alertMessage by sosViewModel.alertMessage.collectAsState()
    val movementState by movementViewModel.movementState.collectAsState()
    val context = LocalContext.current

    // Voice protection state
    val prefs = context.getSharedPreferences("sheshield_prefs", Context.MODE_PRIVATE)
    var isVoiceEnabled by remember {
        mutableStateOf(prefs.getBoolean("voice_protection_enabled", false))
    }

    // Initialize location client
    LaunchedEffect(Unit) {
        sosViewModel.initLocationClient(context)
    }

    // Permission launcher for SOS
    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        permissionsGranted = allGranted

        if (allGranted) {
            sosViewModel.sendSosAlert(context)
        } else {
            sosViewModel.setErrorMessage("⚠️ Permissions required to send SOS alert")
        }
    }

    // Voice permission launcher
    val voicePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            VoiceCommandService.start(context)
            isVoiceEnabled = true
            prefs.edit().putBoolean("voice_protection_enabled", true).apply()
        } else {
            isVoiceEnabled = false
            prefs.edit().putBoolean("voice_protection_enabled", false).apply()
        }
    }

    // Check permissions when countdown finishes
    LaunchedEffect(sosState) {
        if (sosState == SosState.SENT) {
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission && hasLocationPermission) {
                sosViewModel.sendSosAlert(context)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(bottom = 35.dp)
    ) {
        top_bar()

        // --- VOICE PROTECTION CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isVoiceEnabled) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color(0xFFFF9800).copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isVoiceEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Voice Protection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isVoiceEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isVoiceEnabled) "🎤 Active - Say \"Help me\" or \"Emergency\""
                        else "Enable hands-free SOS trigger",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Switch(
                    checked = isVoiceEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            val voicePerms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                voicePerms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                voicePerms.add(Manifest.permission.FOREGROUND_SERVICE)
                            }
                            voicePermissionLauncher.launch(voicePerms.toTypedArray())
                        } else {
                            VoiceCommandService.stop(context)
                            isVoiceEnabled = false
                            prefs.edit().putBoolean("voice_protection_enabled", false).apply()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color(0xFFFF9800),
                        uncheckedTrackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                    )
                )
            }
        }

        // --- ALERT MESSAGES ---
        alertMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("✓") || message.contains("Sent"))
                            Color(0xFF4CAF50) else Color(0xFFFF5252)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // --- MOVEMENT MONITORING STATUS ---
        if (movementState.isActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp),
                onClick = onMovementScreenClick
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = "Monitoring Active",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Movement Detection Active",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Status preserved across navigation",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32).copy(alpha = 0.7f)
                        )
                        if (movementState.lastMovementType.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Last: ${movementState.lastMovementType}",
                                fontSize = 12.sp,
                                color = Color(0xFFF57C00),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = Color(0xFF2E7D32)
                    )
                }
            }
        }

        // --- SOS BUTTON SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (sosState) {
                SosState.IDLE, SosState.CANCELLED -> {
                    SosButton { sosViewModel.startSos() }
                }
                SosState.COUNTDOWN -> {
                    SosCountDown(
                        onFinish = { /* Handled by LaunchedEffect */ },
                        onCancel = { sosViewModel.cancelSos() }
                    )
                }
                SosState.SENDING -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier.padding(horizontal = 25.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Text("Sending SOS Alerts...", fontSize = 18.sp, color = Color.White)
                            Text("SMS • Email • Notifications", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
                SosState.SENT_SUCCESS -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(horizontal = 25.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✓ SOS Alert Sent!", fontSize = 24.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your emergency contacts have been notified", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
                else -> {}
            }
        }

        // --- QUICK ACTION CARDS ---
        Column(
            modifier = Modifier.padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Quick Action", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    cardOne(onClick = onCardOneClick)
                    cardThree()
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    cardTwo(onClick = onCardTwoClick)
                    cardFour()
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 75.dp)
                    .height(170.dp)
            ) {
                cardFive(onClick = onCardFiveClick)
            }

            // Movement Detection Button
            Button(
                onClick = onMovementScreenClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (movementState.isActive) Color(0xFF2E7D32) else Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsRun,
                    "Movement Detection",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (movementState.isActive) "Monitoring Active" else "Movement Detection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (movementState.isActive) "Tap to view details & history" else "Monitor abnormal movements",
                        fontSize = 12.sp
                    )
                }
                if (movementState.isActive) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                }
            }

            safe_box()

            Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun safe_box() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color(0xFFFFBF00), shape = RoundedCornerShape(15.dp))
            .background(color = Color(0xFFFFFDE7), shape = RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Safety",
                modifier = Modifier.padding(4.dp),
                tint = Color(0xFFFFBF00)
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Safety Alert", fontWeight = FontWeight.Medium)
                Text(
                    "Caution advised in Downtown area (8-10 PM). 2 incidents reported this week.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun top_bar() {
    val image = painterResource(R.drawable.shield2)
    Surface(
        color = Color(0xFF6000E9),
        shape = RoundedCornerShape(
            topStart = 0.dp, topEnd = 0.dp, bottomStart = 15.dp, bottomEnd = 15.dp
        ),
        modifier = Modifier.padding(top = 30.dp).fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(all = 10.dp)) {
            Row {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "SheShield",
                        Modifier.padding(bottom = 5.dp),
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "You're protected", color = Color.White, fontSize = 15.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.padding(20.dp),
                    tint = Color.White
                )
            }
            Surface(
                color = Color(0xFF7A4BFA),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(5.dp).fillMaxWidth(1f),
            ) {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = image,
                        contentDescription = "Shield Picture",
                        modifier = Modifier.width(65.dp).height(65.dp)
                    )
                    Column {
                        Text("Safety Status: Active", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("3 trusted contacts. GPS enabled", color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun cardOne(onClick: () -> Unit) {
    val image = painterResource(R.drawable.loc)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = image, contentDescription = "location",
            modifier = Modifier.width(90.dp).height(90.dp)
        )
        Column {
            Text("Track My Route", fontWeight = FontWeight.Medium)
            Text("Share live location", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun cardTwo(onClick: () -> Unit) {
    val image = painterResource(R.drawable.clock)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = image, contentDescription = "Check in",
            modifier = Modifier.width(100.dp).height(100.dp)
        )
        Column {
            Text("Timed Check-In", fontWeight = FontWeight.Medium)
            Text("Set safety timer", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun cardThree() {
    val image = painterResource(R.drawable.electric)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Image(
            painter = image, contentDescription = "sos",
            modifier = Modifier.width(90.dp).height(90.dp)
        )
        Column {
            Text("SOS Trigger", fontWeight = FontWeight.Medium)
            Text("Configure alerts", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun cardFour() {
    val image = painterResource(R.drawable.signal)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Image(
            painter = image, contentDescription = "map",
            modifier = Modifier.width(90.dp).height(90.dp)
        )
        Column {
            Text("Safety Map", fontWeight = FontWeight.Medium)
            Text("View risk areas", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun cardFive(onClick: () -> Unit) {
    val image = painterResource(R.drawable.people)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = image, contentDescription = "Responders",
            modifier = Modifier.width(90.dp).height(90.dp)
        )
        Column {
            Text("Responders Near Me", fontWeight = FontWeight.Medium)
            Text("Find verified helpers", color = Color.Gray, fontSize = 14.sp)
        }
    }
}