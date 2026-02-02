package com.example.sheshield.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var issueType by remember { mutableStateOf("General") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Support") },
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
                "Get help from our support team",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                "We're here to help you with any issues or questions",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Methods Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📞 Contact Methods",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ContactMethodItem(
                        icon = Icons.Default.Email,
                        title = "Email Support",
                        detail = "support@sheshield.com",
                        action = "Send Email"
                    )

                    ContactMethodItem(
                        icon = Icons.Default.Phone,
                        title = "Emergency Hotline",
                        detail = "+880 1712 345678",
                        action = "Call Now"
                    )

                    ContactMethodItem(
                        icon = Icons.Default.Chat,
                        title = "Live Chat",
                        detail = "Available 9 AM - 11 PM",
                        action = "Start Chat"
                    )
                }
            }

            // Send Message Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Send us a message", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Issue Type
                    Text("Issue Type", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("General", "Technical", "Billing", "Safety", "Other").forEach { type ->
                            FilterChip(
                                selected = issueType == type,
                                onClick = { issueType = type },
                                label = { Text(type) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message
                    Text("Your Message", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Describe your issue or question...") },
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Send message */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = message.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Send Message", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Response Time Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Response Time",
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Response Time",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            "We typically respond within 24 hours",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Emergency Note Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚠️ For Emergencies",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF856404)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "If you are in immediate danger:\n• Use the SOS button\n• Call emergency services\n• Contact trusted contacts",
                        fontSize = 14.sp,
                        color = Color(0xFF856404)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactMethodItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(detail, fontSize = 14.sp, color = Color.Gray)
        }
        TextButton(onClick = { /* Action */ }) {
            Text(action, fontWeight = FontWeight.Medium)
        }
    }
}