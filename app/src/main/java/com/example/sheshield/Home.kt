package com.example.sheshield


import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.screens.*

import com.example.sheshield.SOS.*
import com.example.sheshield.SOS.SosViewModel
import com.example.sheshield.viewmodel.MovementViewModel
import com.google.android.gms.location.LocationServices
import com.example.sheshield.R



import com.example.sheshield.services.VoiceCommandService
import com.example.sheshield.services.AudioRecorderService
import com.example.sheshield.screens.TrackRouteScreen


@Composable
fun HomeScreen(
    movementViewModel: MovementViewModel,
    onTrackRouteClick: () -> Unit,
    onTimedCheckInClick: () -> Unit,
    onRespondersClick: () -> Unit,
    onMovementScreenClick: () -> Unit
) {
    HomeContent(
        movementViewModel = movementViewModel,
        sosViewModel = viewModel(),
        onCardOneClick = onTrackRouteClick,
        onCardTwoClick = onTimedCheckInClick,
        onCardFiveClick = onRespondersClick,
        onMovementScreenClick = onMovementScreenClick
    )
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

    // Permission launcher
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
            // Start voice service
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
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val intent = Intent(context, AudioRecorderService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }


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
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF1A1A2E)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(bottom = 35.dp)
    ) {
        top_bar()

        // VOICE PROTECTION CARD - NEW
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isVoiceEnabled) Color(0xFF34D399).copy(alpha = 0.15f)
                else Color(0xFFFBBF24).copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(6.dp),
            shape = RoundedCornerShape(18.dp)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(
                                            if (isVoiceEnabled) Color(0xFF34D399).copy(alpha = 0.3f)
                                            else Color(0xFFFBBF24).copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isVoiceEnabled) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Voice Protection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFE8E8F0) // Light text
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isVoiceEnabled)
                            "🎤 Active - Say \"Help me\" or \"Emergency\""
                        else
                            "Enable hands-free SOS trigger",
                        fontSize = 12.sp,
                        color = Color(0xFFB4B4C8) // Secondary text
                    )
                }

                Switch(
                    checked = isVoiceEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Request voice permissions
                            val voicePerms = mutableListOf(
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                voicePerms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                voicePerms.add(Manifest.permission.FOREGROUND_SERVICE)
                            }

                            voicePermissionLauncher.launch(voicePerms.toTypedArray())
                        } else {
                            // Stop voice service
                            VoiceCommandService.stop(context)
                            isVoiceEnabled = false
                            prefs.edit().putBoolean("voice_protection_enabled", false).apply()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF34D399),
                        checkedTrackColor = Color(0xFF34D399).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color(0xFFFBBF24),
                        uncheckedTrackColor = Color(0xFFFBBF24).copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Show alert message
        alertMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("✓") || message.contains("Sent"))
                            Color(0xFF34D399)
                        else
                            Color(0xFFFB7185)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
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

        // Movement Monitoring Status
        if (movementState.isActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF34D399).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                onClick = onMovementScreenClick
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF34D399).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Monitoring Active",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Movement Detection Active",
                            color = Color(0xFFE8E8F0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Status preserved across navigation",
                            fontSize = 12.sp,
                            color = Color(0xFFB4B4C8)
                        )
                        if (movementState.lastMovementType.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Last: ${movementState.lastMovementType}",
                                fontSize = 12.sp,
                                color = Color(0xFFFBBF24),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = Color(0xFF34D399)
                    )
                }
            }
        }

        // SOS Button Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (sosState) {
                SosState.IDLE, SosState.CANCELLED -> {
                    SosButton {
                        sosViewModel.startSos()
                    }
                }

                SosState.COUNTDOWN -> {
                    SosCountDown(
                        onFinish = {
                            // Will be handled by LaunchedEffect
                        },
                        onCancel = {
                            sosViewModel.cancelSos()
                        }
                    )
                }

                SosState.SENDING -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6B5FEE)),
                        modifier = Modifier.padding(horizontal = 25.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                            Text(
                                "Sending SOS Alerts...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                "SMS • Email • Notifications",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                SosState.SENT_SUCCESS -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF34D399)),
                        modifier = Modifier.padding(horizontal = 25.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "✓ SOS Alert Sent!",
                                fontSize = 24.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your emergency contacts have been notified",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        // Quick Action Cards
        Column(
            modifier = Modifier.padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Quick Action",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE8E8F0) // Light text
            )
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
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (movementState.isActive)
                        Color(0xFF34D399)
                    else
                        Color(0xFF8B7FFF)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
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
                        if (movementState.isActive)
                            "Monitoring Active"
                        else
                            "Movement Detection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (movementState.isActive)
                            "Tap to view details & history"
                        else
                            "Monitor abnormal movements",
                        fontSize = 12.sp
                    )
                }
                if (movementState.isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                }
            }

            safe_box()

            Text(
                "Recent Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE8E8F0) // Light text
            )
        }
    }
}

@Composable
fun safe_box() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBBF24).copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFBBF24).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Safety",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Safety Alert",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "Caution advised in Downtown area (8-10 PM). 2 incidents reported this week.",
                    fontSize = 12.sp,
                    color = Color(0xFFB4B4C8),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun top_bar() {
    val image = painterResource(R.drawable.shield2)
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        ),
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(1f)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                ambientColor = Color(0xFF8B7FFF).copy(alpha = 0.5f),
                spotColor = Color(0xFF8B7FFF).copy(alpha = 0.5f)
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4C3F8F), // Darker purple
                            Color(0xFF3A2F6F)  // Even darker purple
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B7FFF).copy(alpha = 0.6f),
                            Color(0xFF8B7FFF).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    )
                )
        ) {
            Column(Modifier.padding(all = 10.dp)) {
                Row {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "SheShield",
                            Modifier.padding(bottom = 5.dp),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "You're protected",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(44.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .fillMaxWidth(1f),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = image,
                                contentDescription = "Shield Picture",
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF34D399), CircleShape)
                                )
                                Text(
                                    "Safety Status: Active",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "3 trusted contacts • GPS enabled",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun cardOne(onClick: () -> Unit) {
    val image = painterResource(R.drawable.loc)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF25254A)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFF8B7FFF).copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = image,
                    contentDescription = "location",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Track My Route",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "Share live location",
                    color = Color(0xFFB4B4C8),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun cardTwo(onClick: () -> Unit) {
    val image = painterResource(R.drawable.clock)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF25254A)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFF8B7FFF).copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = image,
                    contentDescription = "Check in",
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Timed Check-In",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "Set safety timer",
                    color = Color(0xFFB4B4C8),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun cardThree() {
    val image = painterResource(R.drawable.electric)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF25254A)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFF8B7FFF).copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = image,
                    contentDescription = "sos",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "SOS Trigger",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "Configure alerts",
                    color = Color(0xFFB4B4C8),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun cardFour() {
    val image = painterResource(R.drawable.signal)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF25254A)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFF8B7FFF).copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = image,
                    contentDescription = "map",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Safety Map",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "View risk areas",
                    color = Color(0xFFB4B4C8),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun cardFive(onClick: () -> Unit) {
    val image = painterResource(R.drawable.people)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF25254A)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFF8B7FFF).copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = image,
                    contentDescription = "Responders",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Responders Near Me",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE8E8F0),
                    fontSize = 15.sp
                )
                Text(
                    "Find verified helpers",
                    color = Color(0xFFB4B4C8),
                    fontSize = 13.sp
                )
            }
        }
    }
}