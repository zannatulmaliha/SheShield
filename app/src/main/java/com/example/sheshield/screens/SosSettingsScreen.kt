package com.example.sheshield.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosSettingsScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Load settings from Firebase
    var countdownDuration by remember { mutableStateOf(5) }
    var voiceEnabled by remember { mutableStateOf(true) }
    var powerButtonEnabled by remember { mutableStateOf(true) }
    var sendSmsEnabled by remember { mutableStateOf(true) }
    var sendEmailEnabled by remember { mutableStateOf(true) }
    var sendPushEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    // Load user settings from Firebase
    LaunchedEffect(key1 = auth.currentUser) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        countdownDuration = document.getLong("sosCountdown")?.toInt() ?: 5
                        voiceEnabled = document.getBoolean("sosVoice") ?: true
                        powerButtonEnabled = document.getBoolean("sosPowerButton") ?: true
                        sendSmsEnabled = document.getBoolean("sosSendSms") ?: true
                        sendEmailEnabled = document.getBoolean("sosSendEmail") ?: true
                        sendPushEnabled = document.getBoolean("sosSendPush") ?: true
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Save settings to Firebase
    fun saveSetting(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update(field, value)
            .addOnSuccessListener {
                // Success - settings saved
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6200EE))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Configure your SOS emergency settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    "Customize how SOS alerts work and what happens when triggered",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Countdown Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = Color(0xFF6200EE),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Countdown Duration", fontWeight = FontWeight.Medium)
                                Text("Time before SOS is sent", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Seconds: $countdownDuration", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = countdownDuration.toFloat(),
                            onValueChange = { newValue ->
                                countdownDuration = newValue.toInt()
                                saveSetting("sosCountdown", countdownDuration)
                            },
                            valueRange = 3f..10f,
                            steps = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3s", fontSize = 12.sp, color = Color.Gray)
                            Text("10s", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // Trigger Methods Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Trigger Methods", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice Command
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Voice",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Voice Command", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("Say 'Help me' or 'Emergency'", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = voiceEnabled,
                                onCheckedChange = { enabled ->
                                    voiceEnabled = enabled
                                    saveSetting("sosVoice", enabled)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Power Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PowerSettingsNew,
                                    contentDescription = "Power Button",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Power Button", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("Press 5 times quickly", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = powerButtonEnabled,
                                onCheckedChange = { enabled ->
                                    powerButtonEnabled = enabled
                                    saveSetting("sosPowerButton", enabled)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }

                // Alert Methods Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Alert Methods", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text("How your contacts are notified", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))

                        // SMS Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Sms,
                                    contentDescription = "SMS",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("SMS Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("Text message alerts", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = sendSmsEnabled,
                                onCheckedChange = { enabled ->
                                    sendSmsEnabled = enabled
                                    saveSetting("sosSendSms", enabled)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Email Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("Email notifications", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = sendEmailEnabled,
                                onCheckedChange = { enabled ->
                                    sendEmailEnabled = enabled
                                    saveSetting("sosSendEmail", enabled)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Push Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Push",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Push Notifications", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("App notifications", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = sendPushEnabled,
                                onCheckedChange = { enabled ->
                                    sendPushEnabled = enabled
                                    saveSetting("sosSendPush", enabled)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }

                // What Happens When SOS is Triggered
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📱 What happens when SOS is triggered:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• ${countdownDuration}-second countdown starts", fontSize = 14.sp, color = Color.Gray)
                        Text("• Emergency contacts are notified", fontSize = 14.sp, color = Color.Gray)
                        if (sendSmsEnabled) Text("• SMS sent to trusted contacts", fontSize = 14.sp, color = Color.Gray)
                        if (sendEmailEnabled) Text("• Email sent to trusted contacts", fontSize = 14.sp, color = Color.Gray)
                        if (sendPushEnabled) Text("• Push notifications sent", fontSize = 14.sp, color = Color.Gray)
                        Text("• Your location is shared", fontSize = 14.sp, color = Color.Gray)
                        Text("• Alert is saved to history", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                // Test SOS Button
                // Test SOS Button
                Button(
                    onClick = {
                        // Navigate back to Profile, then to Home to test SOS
                        onBack() // This goes back to Profile
                        // Note: You need additional navigation to go from Profile to Home
                        // This will be handled by the MainActivity navigation
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Test SOS (Go to Home Screen)", fontWeight = FontWeight.Bold)
                }

                // Important Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Important",
                                tint = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "⚠️ Important",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "SOS alerts are for emergencies only. False alerts may result in suspension of service.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}