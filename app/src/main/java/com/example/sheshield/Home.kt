package com.example.sheshield


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.R
import com.example.sheshield.SOS.*
import com.example.sheshield.services.VoiceCommandService
import com.example.sheshield.ui.screens.TimedCheckIn
import com.example.sheshield.screens.TrackRouteScreen

import com.example.sheshield.services.AudioRecorderService
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(sosViewModel: SosViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeContent(
            sosViewModel = sosViewModel,
            onCardOneClick = { currentScreen = "trackRoute" },
            onCardTwoClick = { currentScreen = "timedCheckIn" },
            onCardFiveClick = { currentScreen = "responders" }
        )
        "timedCheckIn" -> TimedCheckIn(
            onNavigate = { currentScreen = it },
            onBack = { currentScreen = "home" }
        )
        "responders" -> RespondersNearMeScreen()
        "trackRoute" -> TrackRouteScreen(
            onBack = { currentScreen = "home" }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeContent(
    sosViewModel: SosViewModel,
    onCardOneClick: () -> Unit,
    onCardTwoClick: () -> Unit,
    onCardFiveClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val sosState by sosViewModel.sosState.collectAsState()
    val alertMessage by sosViewModel.alertMessage.collectAsState()
    val context = LocalContext.current


    // Add debug states
    var isRecordingActive by remember { mutableStateOf(false) }
    var debugMessage by remember { mutableStateOf("") }
    var lastVideoFile by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf("") }


    // Voice protection state
    val prefs = context.getSharedPreferences("sheshield_prefs", Context.MODE_PRIVATE)
    var isVoiceEnabled by remember {
        mutableStateOf(prefs.getBoolean("voice_protection_enabled", false))
    }



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
            // Countdown finished, check permissions and send
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            //video
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            val hasAudioPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED


            //new added for record
    val intent = Intent(context, AudioRecorderService::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }

            if (hasSmsPermission && hasLocationPermission) {
                // All permissions granted, send SOS
                sosViewModel.sendSosAlert(context)

                //VideoRecordingService.startRecording(context)
                //isRecordingActive = true

            } else {
                // Request permissions
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }
        }
//        SosState.SENT_SUCCESS -> {
//        // Video recording is already active
//        sosViewModel.setAlertMessage("✅ SOS sent & emergency recording active")
//    }
//
//        SosState.CANCELLED -> {
//        // Stop video recording if user cancels SOS
//        if (isRecordingActive) {
//            VideoRecordingService.stopRecording(context)
//            isRecordingActive = false
//        }
//    }
//
//        else -> {}
//    }
    }

//    LaunchedEffect(sosState) {
//        when (sosState) {
//            SosState.SENT -> {
//                Log.d("videonmp", "⚠️ Already recording, ignoring")
//                // Countdown finished, check permissions and send
//                val hasSmsPermission = ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.SEND_SMS
//                ) == PackageManager.PERMISSION_GRANTED
//
//                val hasLocationPermission = ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//
//                //video
//                val hasCameraPermission = ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.CAMERA
//                ) == PackageManager.PERMISSION_GRANTED
//
//                val hasAudioPermission = ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.RECORD_AUDIO
//                ) == PackageManager.PERMISSION_GRANTED
//
//                val intent = Intent(context, AudioRecorderService::class.java)
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    context.startForegroundService(intent)
//                } else {
//                    context.startService(intent)
//                }
//
//
//                if (hasSmsPermission && hasLocationPermission && hasCameraPermission && hasAudioPermission) {
//                    // All permissions granted, send SOS
//                    Log.d("videonmp", "⚠️ hAlready recording, ignoring")
//                    sosViewModel.sendSosAlert(context)
//                    //VideoRecordingService.startRecording(context)
//                    SimpleVideoRecorder.startRecording(context)
//
//                } else {
//                    // Request permissions
//                    permissionLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.SEND_SMS,
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.RECORD_AUDIO
//                        )
//                    )
//                }
//            }
//
//            SosState.SENT_SUCCESS -> {
//                // Video recording is already active
//                sosViewModel.setErrorMessage("✅ SOS sent & emergency recording active")
//            }
//
//            SosState.CANCELLED -> {
//                // Stop video recording if user cancels SOS
//
//            }
//
//            else -> {}
//        }
//    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
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
                        if (isVoiceEnabled)
                            "🎤 Active - Say \"Help me\" or \"Emergency\""
                        else
                            "Enable hands-free SOS trigger",
                        fontSize = 12.sp,
                        color = Color.Gray
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
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color(0xFFFF9800),
                        uncheckedTrackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
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
            Text("Quick Action", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    cardOne(onClick = { onCardOneClick() })
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

            Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
            painter = image,
            contentDescription = "location",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
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
            painter = image,
            contentDescription = "Check in",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
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
            painter = image,
            contentDescription = "sos",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
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
            painter = image,
            contentDescription = "map",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
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
            painter = image,
            contentDescription = "Responders",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
        )
        Column {
            Text("Responders near me", fontWeight = FontWeight.Medium)
            Text("Find verified helpers", color = Color.Gray, fontSize = 14.sp)
        }
    }
}