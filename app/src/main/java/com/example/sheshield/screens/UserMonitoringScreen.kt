package com.example.sheshield.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.viewmodel.UserMonitoringViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMonitoringScreen(
    targetUserId: String, // The ID of the user being watched
    targetUserName: String,
    onBack: () -> Unit,
    viewModel: UserMonitoringViewModel = viewModel()
) {
    val context = LocalContext.current
    val targetLocation by viewModel.targetLocation.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()

    // Safety Alert Dialog
    var showAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(targetUserId) {
        viewModel.startMonitoringUser(targetUserId)
    }

    val cameraPositionState = rememberCameraPositionState()

    // Auto-center map on target user
    LaunchedEffect(targetLocation) {
        targetLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Monitoring: $targetUserName", fontSize = 16.sp)
                        Text(
                            if (targetLocation != null) "Live" else "Connecting...",
                            fontSize = 12.sp,
                            color = Color.Green
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF212121),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // MAP VIEW
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                targetLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = targetUserName,
                        snippet = "Last seen: $lastUpdated"
                    )

                    // Visual Danger Zone (Example)
                    Circle(
                        center = loc,
                        radius = 100.0, // meters
                        fillColor = Color.Red.copy(alpha = 0.2f),
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                }
            }

            // DANGER CONTROL PANEL
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Safety Controls",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "If $targetUserName stops responding or enters a danger zone, you can alert nearby helpers.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAlertDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ALERT RESPONDERS (SOS)")
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Confirm Proxy Alert") },
            text = {
                Text("Are you sure? This will alert all SheShield Responders near $targetUserName's last known location.\n\nOnly use this if you believe they are in immediate danger.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.triggerProxySos(targetUserId, targetUserName)
                        showAlertDialog = false
                        Toast.makeText(context, "Alert sent to nearby responders!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("TRIGGER SOS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}