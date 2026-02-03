package com.example.sheshield.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
// ADDED THESE MISSING IMPORTS:
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

private val Green600 = Color(0xFF16A34A)
private val Purple600 = Color(0xFF9333EA)

private val Red600 = Color(0xFFDC2626)
private val Orange600 = Color(0xFFEA580C)
private val Yellow600 = Color(0xFFCA8A04)
private val Gray50 = Color(0xFFF9FAFB)
private val Gray200 = Color(0xFFE5E7EB)

@Composable
fun TimedCheckIn(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    sosViewModel: SosViewModel = viewModel()
) {
    val context = LocalContext.current

    // --- State Management ---
    var isActive by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var timeLeft by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }

    // --- Initialize Location Client ---
    LaunchedEffect(Unit) {
        sosViewModel.initLocationClient(context)
    }

    // --- Permission Handling ---
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

    // Check permissions on launch
    LaunchedEffect(Unit) {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    // --- Focus Management ---
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusRequester.requestFocus()
        }
    }

    // --- Timer Logic ---
    LaunchedEffect(isActive, isPaused, timeLeft) {
        if (isActive && !isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isActive && timeLeft <= 0) {
            isActive = false

            // 1. Send normal SOS from User's phone
            sosViewModel.sendSosAlert(context)

            // 2. Update Firestore so the Monitoring Contact sees it immediately RED
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

    // --- Helper Functions ---
    fun handleStart() {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
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
        targetValue = if (isActive) Orange600 else Purple600,
        label = "HeaderColor"
    )

    Scaffold(
        containerColor = Gray50,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Timed Check-In",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isActive) "Timer active" else "Set a safety countdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
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

// ----------------------------------------------------------------
// Sub-Composables
// ----------------------------------------------------------------

@Composable
fun SetupTimerView(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    onStart: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Set Check-In Time", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("How long until check-in?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            val presets = listOf(15, 30, 45, 60, 90, 120)
            Column {
                val rows = presets.chunked(3)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { mins ->
                            Button(
                                onClick = { onMinutesSelected(mins) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMinutes == mins) Purple600 else Gray50,
                                    contentColor = if (selectedMinutes == mins) Color.White else Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("$mins min")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = selectedMinutes.toString(),
                onValueChange = { if (it.isNotEmpty()) onMinutesSelected(it.toIntOrNull() ?: 0) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Custom Minutes") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Purple600.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "If you don't check in within $selectedMinutes minutes, SOS will be sent automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Start Timer", fontSize = 16.sp)
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
        timeLeft <= 120 -> Red600
        timeLeft <= 300 -> Yellow600
        else -> Green600
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = stateColor),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.Timer, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
            Text(formatTime(timeLeft), fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(if (isPaused) "Paused" else "Time Remaining", color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(if (isPaused) "Resume" else "Pause")
            }
        }
    }

    if (timeLeft <= 120) {
        WarningCard(Red600, "Urgent: Check In Now!", "SOS will trigger soon!")
    } else if (timeLeft <= 300) {
        WarningCard(Yellow600, "Reminder", "Please check in soon.")
    }

    Button(
        onClick = onCheckIn,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("I'm Safe - Check In Now", fontSize = 16.sp)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gray200, contentColor = Color.Gray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Cancel Timer")
    }
}

@Composable
fun WarningCard(color: Color, title: String, text: String) {
    Surface(
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text, color = color.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}