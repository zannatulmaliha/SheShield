package com.example.sheshield.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Frequently Asked Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                "Find answers to common questions about SheShield",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FAQ Items
            FaqItem(
                question = "How do I trigger an SOS alert?",
                answer = "You can trigger SOS by:\n1. Pressing the SOS button on home screen\n2. Saying 'Help me' or 'Emergency' (if voice enabled)\n3. Pressing power button 5 times quickly (if enabled)\nA 5-second countdown will start before alerts are sent."
            )

            FaqItem(
                question = "Who receives my SOS alerts?",
                answer = "Your trusted contacts will receive alerts via SMS, email, and push notifications (based on your settings). They'll get your location and emergency details."
            )

            FaqItem(
                question = "How do I add trusted contacts?",
                answer = "Go to Profile → Safety Settings → Trusted Contacts. Tap 'Add Trusted Contact' and enter their name and phone number."
            )

            FaqItem(
                question = "Is my location always tracked?",
                answer = "No, your location is only shared during SOS alerts and timed check-ins. You can control location sharing in Privacy Settings."
            )

            FaqItem(
                question = "How does voice protection work?",
                answer = "When enabled, SheShield listens for emergency keywords like 'Help me' or 'Emergency'. This feature requires microphone permission and only works when the app is open."
            )

            FaqItem(
                question = "Can I test SOS without alerting contacts?",
                answer = "Yes, go to SOS Settings and tap 'Test SOS'. This will simulate the countdown without sending actual alerts."
            )

            // Quick Links Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Quick Links",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    QuickLinkItem(
                        icon = Icons.Default.Security,
                        title = "Safety Guide",
                        description = "Learn safety tips and best practices"
                    )

                    QuickLinkItem(
                        icon = Icons.Default.Settings,
                        title = "Troubleshooting",
                        description = "Fix common issues and problems"
                    )

                    QuickLinkItem(
                        icon = Icons.Default.QuestionAnswer,
                        title = "Community Forum",
                        description = "Ask questions and share experiences"
                    )
                }
            }

            // Contact Support Button
            Button(
                onClick = { /* Navigate to contact support */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Contact Support", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ArrowBack else Icons.Default.ArrowBack,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(if (expanded) 90f else 270f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    answer,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun QuickLinkItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}