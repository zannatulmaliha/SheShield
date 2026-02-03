package com.example.sheshield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.sheshield.ui.screens.TimedCheckIn
import com.example.sheshield.viewmodel.MovementViewModel
import com.example.sheshield.services.VoiceCommandService

// --- BRAND PALETTE ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentEmerald = Color(0xFF10B981)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentOrange = Color(0xFFEA580C)
private val AccentRed = Color(0xFFDC2626)

@Composable
fun HomeScreen(
    movementViewModel: MovementViewModel,
    sosViewModel: SosViewModel = viewModel(),
    onCardOneClick: () -> Unit,
    onCardTwoClick: () -> Unit,
    onCardFiveClick: () -> Unit,
    onMovementScreenClick: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("home") }
    var showMovementScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    when (currentScreen) {
        "home" -> HomeContent(
            sosViewModel = sosViewModel,
            movementViewModel = movementViewModel,
            onCardOneClick = { currentScreen = "trackRoute" },
            onCardTwoClick = { currentScreen = "timedCheckIn" },
            onCardFiveClick = { currentScreen = "responders" },
            onMovementScreenClick = { showMovementScreen = true }
        )
        "timedCheckIn" -> TimedCheckIn(
            onNavigate = { currentScreen = it },
            onBack = { currentScreen = "home" }
        )
        "responders" -> RespondersNearMeScreen(
            onBackClick = { currentScreen = "home" }
        )
        "trackRoute" -> TrackRouteScreen(
            onBack = { currentScreen = "home" }
        )
    }

    if (showMovementScreen) {
        MovementDetectionScreen(
            onBack = { showMovementScreen = false },
            onAbnormalMovementDetected = { _, _ -> },
            onTriggerSOS = { sosViewModel.sendSosAlert(context) }
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

    val prefs = context.getSharedPreferences("sheshield_prefs", Context.MODE_PRIVATE)
    var isVoiceEnabled by remember { mutableStateOf(prefs.getBoolean("voice_protection_enabled", false)) }

    LaunchedEffect(Unit) { sosViewModel.initLocationClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) sosViewModel.sendSosAlert(context)
        else sosViewModel.setErrorMessage("⚠️ Permissions required")
    }

    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            VoiceCommandService.start(context)
            isVoiceEnabled = true
            prefs.edit().putBoolean("voice_protection_enabled", true).apply()
        }
    }

    LaunchedEffect(sosState) {
        if (sosState == SosState.SENT) {
            val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
            val hasLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasSms && hasLoc) sosViewModel.sendSosAlert(context)
            else permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MidnightBase)) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).padding(bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            top_bar()

            // --- VOICE PROTECTION CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isVoiceEnabled) AccentEmerald.copy(0.1f) else GlassWhite)
                    .border(1.dp, if (isVoiceEnabled) AccentEmerald.copy(0.4f) else GlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, null, tint = if (isVoiceEnabled) AccentEmerald else AccentOrange)
                            Spacer(Modifier.width(8.dp))
                            Text("Voice Protection", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            if (isVoiceEnabled) "Active - Monitoring for keywords" else "Hands-free SOS trigger",
                            fontSize = 12.sp, color = Color.White.copy(0.6f)
                        )
                    }
                    Switch(
                        checked = isVoiceEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val voicePerms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) voicePerms.add(Manifest.permission.POST_NOTIFICATIONS)
                                voicePermissionLauncher.launch(voicePerms.toTypedArray())
                            } else {
                                VoiceCommandService.stop(context)
                                isVoiceEnabled = false
                                prefs.edit().putBoolean("voice_protection_enabled", false).apply()
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentEmerald, checkedTrackColor = AccentEmerald.copy(0.5f))
                    )
                }
            }

            // --- ALERT MESSAGES ---
            alertMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (message.contains("✓")) AccentEmerald else AccentRed),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(message, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                }
            }

            // --- MOVEMENT STATUS ---
            if (movementState.isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentEmerald.copy(0.15f))
                        .border(1.dp, AccentEmerald.copy(0.3f), RoundedCornerShape(16.dp))
                        .clickable { onMovementScreenClick() }
                        .padding(15.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = AccentEmerald)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Movement Monitoring", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Last: ${movementState.lastMovementType}", color = AccentEmerald, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                    }
                }
            }

            // --- SOS BUTTON ---
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                when (sosState) {
                    SosState.IDLE, SosState.CANCELLED -> SosButton { sosViewModel.startSos() }
                    SosState.COUNTDOWN -> SosCountDown(onFinish = {}, onCancel = { sosViewModel.cancelSos() })
                    SosState.SENDING -> CircularProgressIndicator(color = AccentPurple)
                    SosState.SENT_SUCCESS -> Text("✓ SOS SENT", color = AccentEmerald, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    else -> {}
                }
            }

            // --- QUICK ACTIONS ---
            Column(modifier = Modifier.padding(25.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Quick Actions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        cardOne(onClick = onCardOneClick)
                        cardThree()
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        cardTwo(onClick = onCardTwoClick)
                        cardFour()
                    }
                }
                cardFive(onClick = onCardFiveClick)

                // Movement Detection Button
                Button(
                    onClick = onMovementScreenClick,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (movementState.isActive) AccentEmerald else AccentPurple),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (movementState.isActive) "Monitoring Active" else "Enable Detection", fontWeight = FontWeight.Bold)
                }

                safe_box()
                Text("Recent Activity", color = Color.White.copy(0.5f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun safe_box() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentOrange.copy(0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, AccentOrange.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row {
            Icon(Icons.Default.Notifications, null, tint = AccentOrange)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Safety Alert", color = AccentOrange, fontWeight = FontWeight.Bold)
                Text("Caution: High incident rate in current area.", color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun top_bar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase.copy(0.8f))))
            .padding(top = 45.dp, bottom = 25.dp, start = 25.dp, end = 25.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SheShield", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("You're protected", color = AccentEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {}, modifier = Modifier.background(GlassWhite, CircleShape)) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite, RoundedCornerShape(20.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.shield2), null, Modifier.size(50.dp))
                    Spacer(Modifier.width(15.dp))
                    Column {
                        Text("Safety Status: Active", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("3 trusted contacts linked", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- CARD COMPONENTS (Glassified) ---

@Composable
fun cardOne(onClick: () -> Unit) { GlassCard(R.drawable.loc, "Track Route", "Share location", onClick) }
@Composable
fun cardTwo(onClick: () -> Unit) { GlassCard(R.drawable.clock, "Timed Check-In", "Set safety timer", onClick) }
@Composable
fun cardThree() { GlassCard(R.drawable.electric, "SOS Trigger", "Configure alerts", {}) }
@Composable
fun cardFour() { GlassCard(R.drawable.signal, "Analytics", "Monitor patterns", {}) }
@Composable
fun cardFive(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.people), null, Modifier.size(60.dp))
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Responders Near Me", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Find verified helpers", color = Color.White.copy(0.6f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun GlassCard(imgRes: Int, title: String, sub: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Image(painterResource(imgRes), null, Modifier.size(60.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(sub, color = Color.White.copy(0.5f), fontSize = 11.sp, maxLines = 1)
        }
    }
}