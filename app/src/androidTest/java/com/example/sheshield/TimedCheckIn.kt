package com.example.sheshield

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Optional
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.text.format
import kotlin.text.isNotEmpty
import kotlin.text.toIntOrNull

// Define your specific colors to match the design
val Purple600 = Color(0xFF9333EA)
val Green600 = Color(0xFF16A34A)
val Red600 = Color(0xFFDC2626)
val Orange600 = Color(0xFFEA580C)
val Yellow600 = Color(0xFFCA8A04)
val Gray50 = Color(0xFFF9FAFB)
val Gray200 = Color(0xFFE5E7EB)

@Composable
fun TimedCheckIn(
    onNavigate: (String) -> Unit
) {
    // --- State Management ---
    var isActive by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var timeLeft by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }

    // --- Timer Logic ---
    LaunchedEffect(isActive, isPaused, timeLeft) {
        if (isActive && !isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isActive && timeLeft <= 0) {
            isActive = false
            // Handle Timer Expired Logic
            onNavigate("sos-active")
        }
    }

    // --- Helper Functions ---
    fun handleStart() {
        timeLeft = selectedMinutes * 60L
        isActive = true
        isPaused = false
    }

    fun handleCheckIn() {
        isActive = false
        timeLeft = 0
        onNavigate("dashboard")
    }

    fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    // Dynamic Header Color
    val headerColor by animateColorAsState(
        targetValue = if (isActive) Orange600 else Purple600,
        label = "HeaderColor"
    )

    Scaffold(
        containerColor = Gray50,
        topBar = {
            // Custom Header
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
                        text = if (isActive) "Timer active - Check in before it expires" else "Set a safety countdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                IconButton(onClick = { onNavigate("dashboard") }) {
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
                // --- SETUP VIEW ---
                SetupTimerView(
                    selectedMinutes = selectedMinutes,
                    onMinutesSelected = { selectedMinutes = it },
                    onStart = { handleStart() }
                )
            } else {
                // --- ACTIVE TIMER VIEW ---
                ActiveTimerView(
                    timeLeft = timeLeft,
                    totalTime = selectedMinutes * 60L,
                    isPaused = isPaused,
                    onTogglePause = { isPaused = !isPaused },
                    onCheckIn = { handleCheckIn() },
                    onCancel = {
                        isActive = false
                        onNavigate("dashboard")
                    },
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
    // Time Selection Card
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = Optional.ofNullable(null).orElse(null) // visual fix
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Set Check-In Time", style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Text("How long until check-in?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            // Grid of buttons
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

            // Custom Input
            Text("Or enter custom time (minutes)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            OutlinedTextField(
                value = selectedMinutes.toString(),
                onValueChange = {
                    if (it.isNotEmpty()) onMinutesSelected(it.toIntOrNull() ?: 0)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Box
            Surface(
                color = Purple600.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "You'll need to check in within $selectedMinutes minutes. If you don't, an SOS alert will be triggered automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // How It Works Section
    Surface(
        color = Color(0xFFEFF6FF), // Blue 50
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("How It Works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            InstructionItem("1", "Set your check-in time and start the timer")
            InstructionItem("2", "The app will remind you to check in before time expires")
            InstructionItem("3", "If you don't check in, SOS is triggered automatically")
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Start Button
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
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
    // Determine Color state based on time left
    val stateColor = when {
        timeLeft <= 120 -> Red600
        timeLeft <= 300 -> Yellow600
        else -> Green600
    }

    // Main Timer Card
    Card(
        colors = CardDefaults.cardColors(containerColor = stateColor),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.Timer, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formatTime(timeLeft),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaused) Color.White.copy(alpha = 0.5f) else Color.White
            )

            Text(
                text = if (isPaused) "Timer Paused" else "Time remaining until check-in",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (timeLeft.toFloat() / totalTime.toFloat()) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pause/Resume Button
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPaused) "Resume" else "Pause", color = Color.White)
            }
        }
    }

    // Warnings
    if (timeLeft <= 120) {
        WarningCard(
            color = Red600,
            title = "Urgent: Check In Now!",
            text = "Less than 2 minutes left. SOS will trigger automatically if you don't check in."
        )
    } else if (timeLeft <= 300) {
        WarningCard(
            color = Yellow600,
            title = "Reminder",
            text = "Less than 5 minutes remaining. Please check in soon."
        )
    }

    // "What Happens" Section
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("What happens when timer expires?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            val orangeIcon = Orange600
            InfoItem(Icons.Default.Warning, orangeIcon, "Automatic SOS alert activated")
            InfoItem(Icons.Default.Notifications, orangeIcon, "All trusted contacts notified")
            InfoItem(Icons.Default.LocationOn, orangeIcon, "Live location sharing begins")
            InfoItem(Icons.Default.LocalHospital, orangeIcon, "Emergency services may be contacted")
        }
    }

    // Action Buttons
    Button(
        onClick = onCheckIn,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("I'm Safe - Check In Now", fontSize = 16.sp)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gray200, contentColor = Color.Gray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Cancel Timer", fontSize = 14.sp)
    }
}

// ----------------------------------------------------------------
// UI Components Helpers
// ----------------------------------------------------------------

@Composable
fun InstructionItem(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFFDBEAFE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun InfoItem(icon: ImageVector, tint: Color, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun WarningCard(color: Color, title: String, text: String) {
    Surface(
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
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