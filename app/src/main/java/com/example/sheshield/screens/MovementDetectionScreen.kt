package com.example.sheshield.screens

import android.Manifest
import android.os.Build
import android.os.CountDownTimer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.services.MovementLogEntry
import com.example.sheshield.viewmodel.MovementViewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// --- Interface for SOS Trigger ---
// You likely have this logic elsewhere, passing it in as a lambda
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovementDetectionScreen(
    onBack: () -> Unit,
    onAbnormalMovementDetected: (type: String, confidence: Float) -> Unit,
    onTriggerSOS: (String) -> Unit // New parameter for SOS
) {
    val context = LocalContext.current
    val movementViewModel: MovementViewModel = viewModel()

    // Collect state from ViewModel
    val movementState by movementViewModel.movementState.collectAsState()
    val permissionState by movementViewModel.permissionState.collectAsState()

    // State for SOS Countdown Dialog
    var showSOSCountdown by remember { mutableStateOf(false) }
    var sosCountdownValue by remember { mutableIntStateOf(5) }
    var detectedAnomalyType by remember { mutableStateOf("") }
    var sosTimer: CountDownTimer? by remember { mutableStateOf(null) }

    // Function to start SOS countdown
    fun startSOSCountdown(anomalyType: String) {
        if (showSOSCountdown) return // Already showing

        detectedAnomalyType = anomalyType
        showSOSCountdown = true
        sosCountdownValue = 5

        sosTimer?.cancel()
        sosTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sosCountdownValue = (millisUntilFinished / 1000).toInt() + 1
            }

            override fun onFinish() {
                showSOSCountdown = false
                // Trigger the actual SOS
                onTriggerSOS("Automatic SOS detected: $anomalyType")
            }
        }.start()
    }

    fun cancelSOS() {
        sosTimer?.cancel()
        showSOSCountdown = false
    }

    // State for log filter
    var showLogFilterDialog by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.LAST_HOUR) }
    var filteredLogs by remember { mutableStateOf<List<MovementLogEntry>>(emptyList()) }

    // Request activity recognition permission
    val permissionStateRequest = rememberPermissionState(
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    // Request body sensors permission
    val bodySensorsPermissionState = rememberPermissionState(
        Manifest.permission.BODY_SENSORS
    )

    // Request location permission
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Initialize on first composition
    LaunchedEffect(Unit) {
        movementViewModel.initialize(context)
        movementViewModel.setAbnormalMovementCallback { type, confidence ->
            onAbnormalMovementDetected(type, confidence)

            // AUTOMATIC SOS TRIGGER LOGIC
            // Trigger if confidence is high (> 0.8) for critical events
            if (confidence > 0.8f && (type == "SPRINT" || type == "UNUSUAL_ROTATION" || type == "SUDDEN_HALT")) {
                startSOSCountdown(type)
            }
        }
    }

    // Update filtered logs when state changes
    LaunchedEffect(movementState.log, selectedTimeRange) {
        filteredLogs = filterLogsByTimeRange(movementState.log, selectedTimeRange)
    }

    // Check and request permissions when monitoring starts
    LaunchedEffect(movementState.isActive) {
        if (movementState.isActive && !permissionState.hasRequiredPermissions) {
            // Request permissions sequentially
            val permissionsToRequest = mutableListOf<suspend () -> Unit>()

            if (!permissionStateRequest.status.isGranted) {
                permissionsToRequest.add { permissionStateRequest.launchPermissionRequest() }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !bodySensorsPermissionState.status.isGranted) {
                permissionsToRequest.add { bodySensorsPermissionState.launchPermissionRequest() }
            }

            if (!locationPermissionState.allPermissionsGranted) {
                permissionsToRequest.add { locationPermissionState.launchMultiplePermissionRequest() }
            }

            for (request in permissionsToRequest) {
                request()
                delay(300)
            }

            // Update ViewModel with permission status
            delay(500)
            movementViewModel.updatePermissionState(
                Manifest.permission.ACTIVITY_RECOGNITION,
                permissionStateRequest.status.isGranted
            )
            movementViewModel.updatePermissionState(
                Manifest.permission.BODY_SENSORS,
                bodySensorsPermissionState.status.isGranted
            )
            movementViewModel.updatePermissionState(
                Manifest.permission.ACCESS_FINE_LOCATION,
                locationPermissionState.allPermissionsGranted
            )

            // Stop monitoring if no permissions granted
            if (!permissionStateRequest.status.isGranted && !bodySensorsPermissionState.status.isGranted) {
                movementViewModel.stopMonitoring()
            }
        }
    }

    // --- SOS Countdown Dialog ---
    if (showSOSCountdown) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss by tapping outside */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Detected!", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Abnormal movement detected: $detectedAnomalyType")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$sosCountdownValue",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text("Sending SOS in seconds...")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cancelSOS() // Cancel timer first
                        onTriggerSOS("Manual SOS confirmation: $detectedAnomalyType")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SEND NOW")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cancelSOS() }) {
                    Text("I'M SAFE (CANCEL)")
                }
            },
            containerColor = Color(0xFFFFEBEE)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Movement Detection",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Clear Logs Button
                    if (movementState.log.isNotEmpty()) {
                        IconButton(
                            onClick = { movementViewModel.clearLog() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear All Logs",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (movementState.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (movementState.isActive) "ACTIVE MONITORING" else "INACTIVE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (movementState.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (movementState.isActive)
                            "Detecting abnormal movements..."
                        else
                            "Press start to begin monitoring",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            // Start/Stop Button
            Button(
                onClick = { movementViewModel.toggleMonitoring() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (movementState.isActive) Color(0xFFF44336) else Color(0xFF4CAF50)
                ),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = if (movementState.isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (movementState.isActive) "Stop" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (movementState.isActive) "STOP MONITORING" else "START MONITORING",
                    fontWeight = FontWeight.Bold
                )
            }

            // Last Detection - Only show if it's recent (within last 5 minutes)
            val lastLog = movementState.log.lastOrNull()
            val showLastDetection = lastLog != null &&
                    (System.currentTimeMillis() - lastLog.timestamp < 5 * 60 * 1000)

            if (showLastDetection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8E1)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFFF9800)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                text = "Recent Detection:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${formatTimeAgo(lastLog!!.timestamp)} ago",
                                fontSize = 12.sp,
                                color = Color(0xFFF57C00).copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getMovementTypeDisplayName(lastLog.type),
                            fontSize = 18.sp,
                            color = Color(0xFFF57C00),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Confidence: ${"%.0f".format(lastLog.confidence * 100)}%",
                                color = Color(0xFFF57C00)
                            )
                            Text(
                                text = formatTimestamp(lastLog.timestamp),
                                fontSize = 12.sp,
                                color = Color(0xFFF57C00).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else if (movementState.isActive) {
                // Show monitoring active message instead
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Monitoring",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monitoring Active",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No abnormal movements detected recently",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Movement Log Section
            if (movementState.log.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Takes remaining space
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header with filter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Movement Log",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Showing ${filteredLogs.size} of ${movementState.log.size} entries",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Filter Button
                            IconButton(
                                onClick = { showLogFilterDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        if (filteredLogs.isEmpty()) {
                            // Empty state for filtered logs
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.FilterAltOff,
                                        contentDescription = "No filtered logs",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No logs in selected time range",
                                        color = Color.Gray
                                    )
                                    TextButton(
                                        onClick = { selectedTimeRange = TimeRange.ALL }
                                    ) {
                                        Text("Show All Logs")
                                    }
                                }
                            }
                        } else {
                            // Log List with Numbers - FIXED: Using proper LazyColumn
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f) // Takes remaining space
                                    .padding(horizontal = 16.dp)
                            ) {
                                itemsIndexed(filteredLogs.reversed()) { index, log ->
                                    val displayIndex = filteredLogs.size - index

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Number
                                        Text(
                                            text = displayIndex.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF6200EE),
                                            modifier = Modifier.width(30.dp)
                                        )

                                        // Type with icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            val (icon, color) = getMovementTypeIconAndColor(log.type)

                                            Icon(
                                                icon,
                                                contentDescription = log.type,
                                                tint = color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = getMovementTypeDisplayName(log.type),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Time
                                        Text(
                                            text = formatTimestamp(log.timestamp),
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Confidence
                                        Box(
                                            modifier = Modifier
                                                .width(50.dp)
                                                .height(20.dp)
                                                .background(
                                                    getConfidenceColor(log.confidence),
                                                    RoundedCornerShape(4.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${"%.0f".format(log.confidence * 100)}%",
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (index < filteredLogs.size - 1) {
                                        Divider(
                                            color = Color.Gray.copy(alpha = 0.1f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }

                                // Add some bottom padding
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty State
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, "No logs", tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No movement logs yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Start monitoring to detect and log abnormal movements", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    // Log Filter Dialog
    if (showLogFilterDialog) {
        AlertDialog(
            onDismissRequest = { showLogFilterDialog = false },
            title = { Text("Filter Logs") },
            text = {
                Column {
                    Text("Show logs from:", modifier = Modifier.padding(bottom = 8.dp))
                    TimeRange.entries.forEach { range ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTimeRange = range }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedTimeRange == range, onClick = { selectedTimeRange = range })
                            Text(text = range.displayName, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogFilterDialog = false }) { Text("Close") }
            }
        )
    }
}

// ... (Rest of your helper functions: TimeRange, filterLogsByTimeRange, countLogsInRange, formatTimestamp, formatTimeAgo, getMovementTypeDisplayName, getMovementTypeIconAndColor, getConfidenceColor stay the same)
enum class TimeRange(val displayName: String, val durationMs: Long?) {
    LAST_HOUR("Last Hour", 60 * 60 * 1000L),
    LAST_24_HOURS("Last 24 Hours", 24 * 60 * 60 * 1000L),
    LAST_WEEK("Last 7 Days", 7 * 24 * 60 * 60 * 1000L),
    LAST_MONTH("Last 30 Days", 30L * 24 * 60 * 60 * 1000L),
    ALL("All Time", null)
}

private fun filterLogsByTimeRange(logs: List<MovementLogEntry>, timeRange: TimeRange): List<MovementLogEntry> {
    return if (timeRange.durationMs == null) {
        logs
    } else {
        val cutoff = System.currentTimeMillis() - timeRange.durationMs
        logs.filter { it.timestamp >= cutoff }
    }
}

private fun countLogsInRange(logs: List<MovementLogEntry>, timeRange: TimeRange): Int {
    return if (timeRange.durationMs == null) {
        logs.size
    } else {
        val cutoff = System.currentTimeMillis() - timeRange.durationMs
        logs.count { it.timestamp >= cutoff }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "${diff / 1000} sec"
        diff < 3600000 -> "${diff / 60000} min"
        diff < 86400000 -> "${diff / 3600000} hr"
        diff < 604800000 -> "${diff / 86400000} day"
        diff < 2592000000L -> "${diff / 604800000} wk"
        else -> "${diff / 2592000000L} mo"
    }
}

private fun getMovementTypeDisplayName(type: String): String {
    return when (type) {
        "SPRINT" -> "Sudden Sprint"
        "SUDDEN_HALT" -> "Sudden Halt"
        "PROLONGED_STATIONARY" -> "Prolonged Stationary"
        "UNUSUAL_ROTATION" -> "Unusual Rotation"
        "RUNNING_PATTERN" -> "Running Pattern"
        else -> type
    }
}

private fun getMovementTypeIconAndColor(type: String): Pair<ImageVector, Color> {
    return when (type) {
        "SPRINT" -> Pair(Icons.Default.DirectionsRun, Color(0xFFF44336))
        "SUDDEN_HALT" -> Pair(Icons.Default.Pause, Color(0xFFFF9800))
        "PROLONGED_STATIONARY" -> Pair(Icons.Default.Timer, Color(0xFF2196F3))
        "UNUSUAL_ROTATION" -> Pair(Icons.Default.Cached, Color(0xFF9C27B0))
        "RUNNING_PATTERN" -> Pair(Icons.Default.DirectionsRun, Color(0xFF4CAF50))
        else -> Pair(Icons.Default.Info, Color.Gray)
    }
}

private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> Color(0xFF4CAF50) // Green
        confidence >= 0.6f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}