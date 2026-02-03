package com.example.sheshield.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.SOS.SosViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

// --- BRAND PALETTE ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val AccentOrange = Color(0xFFF59E0B)
private val AccentRed = Color(0xFFEF4444)

@Composable
fun TimedCheckIn(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    sosViewModel: SosViewModel = viewModel()
) {
    val context = LocalContext.current

    var isActive by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var timeLeft by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sosViewModel.initLocationClient(context)
    }

    val requiredPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
            val locGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (smsGranted && locGranted) {
                Toast.makeText(context, "Safety features enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissions needed for SOS alerts", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(isActive, isPaused, timeLeft) {
        if (isActive && !isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isActive && timeLeft <= 0) {
            isActive = false
            sosViewModel.sendSosAlert(context)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                FirebaseFirestore.getInstance().collection("active_sessions")
                    .document(currentUser.uid)
                    .update("status", "MISSED_CHECK_IN")
            }
            onNavigate("sos-active")
            onBack()
        }
    }

    fun handleStart() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            timeLeft = selectedMinutes * 60L
            isActive = true
            isPaused = false
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    fun handleCheckIn() {
        onBack()
    }

    fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    val headerColor by animateColorAsState(
        targetValue = if (isActive) AccentOrange else AccentPurple,
        label = "HeaderColor"
    )

    Scaffold(
        containerColor = MidnightBase,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (isActive && event.type == KeyEventType.KeyDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_VOLUME_UP -> {
                            timeLeft += 60
                            true
                        }
                        android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            timeLeft = (timeLeft - 60).coerceAtLeast(0)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase.copy(0.8f))))
                    .padding(top = 45.dp, bottom = 25.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onBack() },
                        modifier = Modifier.background(GlassWhite, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Timed Check-In", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(
                            if (isActive) "Timer active" else "Set safety countdown",
                            color = headerColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (!isActive) {
                SetupTimerView(
                    selectedMinutes = selectedMinutes,
                    onMinutesSelected = { selectedMinutes = it },
                    onStart = { handleStart() }
                )
            } else {
                ActiveTimerView(
                    timeLeft = timeLeft,
                    totalTime = selectedMinutes * 60L,
                    isPaused = isPaused,
                    onTogglePause = { isPaused = !isPaused },
                    onCheckIn = { handleCheckIn() },
                    onCancel = { onBack() },
                    formatTime = { formatTime(it) }
                )
            }
        }
    }
}

@Composable
fun SetupTimerView(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite, RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Text("Set Check-In Time", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            val presets = listOf(15, 30, 45, 60, 90, 120)
            presets.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { mins ->
                        Button(
                            onClick = { onMinutesSelected(mins) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMinutes == mins) AccentPurple else GlassWhite,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("$mins min", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = selectedMinutes.toString(),
                onValueChange = { if (it.isNotEmpty()) onMinutesSelected(it.toIntOrNull() ?: 0) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Custom Minutes", color = Color.White.copy(0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = GlassBorder
                )
            )

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentPurple.copy(0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "SOS will trigger automatically if you don't check in within $selectedMinutes mins.",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Start Safety Timer", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActiveTimerView(
    timeLeft: Long,
    totalTime: Long,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    formatTime: (Long) -> String
) {
    val stateColor = when {
        timeLeft <= 120 -> AccentRed
        timeLeft <= 300 -> AccentOrange
        else -> AccentEmerald
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(stateColor.copy(0.15f), RoundedCornerShape(28.dp))
            .border(2.dp, stateColor.copy(0.4f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Timer, null, tint = stateColor, modifier = Modifier.size(48.dp))
            Text(formatTime(timeLeft), fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(if (isPaused) "PAUSED" else "TIME REMAINING", color = Color.White.copy(0.5f), letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = stateColor,
                trackColor = GlassWhite
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isPaused) "Resume" else "Pause", color = Color.White)
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    if (timeLeft <= 120) {
        WarningCard(AccentRed, "Urgent: Check In Now!", "SOS will trigger soon!")
    } else if (timeLeft <= 300) {
        WarningCard(AccentOrange, "Reminder", "Please check in soon.")
    }

    Button(
        onClick = onCheckIn,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Default.Shield, null)
        Spacer(Modifier.width(8.dp))
        Text("I'M SAFE - CHECK IN", fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.height(12.dp))
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cancel Timer", color = Color.White.copy(0.5f))
    }
}

@Composable
fun WarningCard(color: Color, title: String, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(color.copy(0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text, color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        }
    }
}