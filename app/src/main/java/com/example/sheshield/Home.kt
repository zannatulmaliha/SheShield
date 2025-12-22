package com.example.sheshield

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.SOS.*
import com.example.sheshield.screens.TrackRouteScreen // Import the new screen
import com.example.sheshield.SOS.SosViewModel
import com.google.android.gms.location.LocationServices

@Composable
fun HomeScreen(sosViewModel: SosViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeContent(
            sosViewModel = sosViewModel,
            onCardOneClick = { currentScreen = "trackRoute" }, // Add this
            onCardTwoClick = { currentScreen = "timedCheckIn" },
            onCardFiveClick = { currentScreen = "responders" }
        )
        "timedCheckIn" -> TimedCheckIn(
            onNavigate = { currentScreen = it },
            onBack = { currentScreen = "home" }

        )
        "responders" -> RespondersNearMeScreen()
        "trackRoute" -> TrackRouteScreen( // Add this state
            onBack = { currentScreen = "home" }
        )
    }
}

@Composable
fun HomeContent(
    sosViewModel: SosViewModel,
    onCardOneClick: () -> Unit, // Add param
    onCardTwoClick: () -> Unit,
    onCardFiveClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val sosState by sosViewModel.sosState.collectAsState()
    val alertMessage by sosViewModel.alertMessage.collectAsState()
    val context = LocalContext.current

    // Initialize location client
    LaunchedEffect(Unit) {
        sosViewModel.initLocationClient(context)
    }

    // Permission launcher - requests all permissions at once
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        permissionsGranted = allGranted

        if (allGranted) {
            // Permissions granted, send SOS immediately
            sosViewModel.sendSosAlert(context)
        } else {
            // Show error message
            sosViewModel.setErrorMessage("⚠️ Permissions required to send SOS alert")
        }
    }

    // Check permissions when countdown finishes
    LaunchedEffect(sosState) {
        if (sosState == SosState.SENT) {
            // Countdown finished, check permissions and send
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission && hasLocationPermission) {
                // All permissions granted, send SOS
                sosViewModel.sendSosAlert(context)
            } else {
                // Request permissions
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
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFF5252)
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
                            // This is called by the countdown component
                            // The LaunchedEffect above will handle sending
                        },
                        onCancel = {
                            sosViewModel.cancelSos()
                        }
                    )
                }

                SosState.SENDING -> {
                    // Show sending indicator
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
                            Text(
                                "Sending SOS Alerts...",
                                fontSize = 18.sp,
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
                    // Show success message (auto-dismissed by ViewModel)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(horizontal = 25.dp)
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

        Column(
            modifier = Modifier.padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Quick Action", fontSize = 18.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    cardOne(onClick = { onCardOneClick() }) // Pass click listener
                    cardThree()
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    cardTwo(onClick = { onCardTwoClick() })
                    cardFour()
                }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 75.dp)
                    .height(170.dp)
            ) {
                cardFive(onClick = { onCardFiveClick() })
            }

            safe_box()

            Text("Recent Activity", fontSize = 18.sp)
        }
    }
}

@Composable
fun safe_box() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFFFBF00),
                shape = RoundedCornerShape(15.dp)
            )
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
                Text("Safety Alert")
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
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 15.dp,
            bottomEnd = 15.dp
        ),
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(all = 10.dp)) {
            Row {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "SheShield",
                        Modifier.padding(bottom = 5.dp),
                        color = Color.White,
                        fontSize = 25.sp
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
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 10.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                ),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f),
            ) {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = image,
                        contentDescription = "Shield Picture",
                        modifier = Modifier
                            .width(65.dp)
                            .height(65.dp)
                    )
                    Column {
                        Text("Safety Status: Active", color = Color.White)
                        Text("3 trusted contacts. GPS enabled", color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun cardOne(onClick: () -> Unit) { // Added onClick parameter
    val image = painterResource(R.drawable.loc)
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(15.dp))
            .padding(10.dp)
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() } // Make clickable
    ) {
        Image(
            painter = image,
            contentDescription = "location",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
        )
        Column {
            Text("Track My Route")
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
            painter = image,
            contentDescription = "Check in",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
        )
        Column {
            Text("Timed Check-In")
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
            painter = image,
            contentDescription = "sos",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
        )
        Column {
            Text("SOS Trigger")
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
            painter = image,
            contentDescription = "map",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
        )
        Column {
            Text("Safety Map")
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
            painter = image,
            contentDescription = "Responders",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
        )
        Column {
            Text("Responders near me")
            Text("Find verified helpers", color = Color.Gray, fontSize = 14.sp)
        }
    }
}