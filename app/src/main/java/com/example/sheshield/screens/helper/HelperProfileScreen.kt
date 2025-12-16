package com.example.sheshield.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    userData: UserData?
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var responseRadius by remember { mutableStateOf(userData?.responseRadius ?: 5) }
    var isReceiveHighRisk by remember { mutableStateOf(true) }
    var isReceiveMediumRisk by remember { mutableStateOf(true) }
    var isReceiveLowRisk by remember { mutableStateOf(false) }
    var activeHoursStart by remember { mutableStateOf("08:00") }
    var activeHoursEnd by remember { mutableStateOf("22:00") }

    // Update Firestore when settings change
    fun updateSetting(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update(field, value)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Helper Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar with Gender Badge
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Main avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    when(userData?.gender) {
                                        "male" -> Color(0xFF1976D2)
                                        "female" -> Color(0xFFE91E63)
                                        else -> Color(0xFF9C27B0)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when(userData?.gender) {
                                    "male" -> Icons.Default.Male
                                    "female" -> Icons.Default.Female
                                    else -> Icons.Default.Person
                                },
                                "Profile",
                                modifier = Modifier.size(50.dp),
                                tint = Color.White
                            )
                        }

                        // Verification badge
                        if (userData?.isHelperVerified == true) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 30.dp, y = 30.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .size(30.dp)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        userData?.name ?: "Helper",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        userData?.email ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // User Type & Gender Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when(userData?.userType) {
                                        "helper" -> Color(0xFF1976D2)
                                        "user_helper" -> Color(0xFF4CAF50)
                                        else -> Color(0xFF6200EE)
                                    },
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                when(userData?.userType) {
                                    "helper" -> "HELPER ONLY"
                                    "user_helper" -> "USER & HELPER"
                                    else -> "USER"
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    when(userData?.gender) {
                                        "male" -> Color(0xFF1976D2)
                                        "female" -> Color(0xFFE91E63)
                                        else -> Color(0xFF9C27B0)
                                    },
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                userData?.gender?.uppercase() ?: "USER",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Verification Status
                    if (userData?.isHelperVerified == true) {
                        Row(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                "Verified",
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verified Helper", color = Color(0xFF4CAF50))
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                "Verification Pending",
                                color = Color(0xFFF44336),
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { /* Request verification */ },
                                modifier = Modifier.padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2)
                                )
                            ) {
                                Text("Request Verification")
                            }
                        }
                    }
                }
            }

            // Helper Statistics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Helper Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level Progress
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Level ${userData?.helperLevel ?: 1}")
                        LinearProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .height(8.dp),
                            color = Color(0xFF1976D2),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Text("Level ${(userData?.helperLevel ?: 1) + 1}")
                    }
                    Text(
                        "75% to next level",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox(
                            value = "24",
                            label = "Total\nResponses",
                            color = Color(0xFF1976D2)
                        )
                        StatBox(
                            value = "91%",
                            label = "Success\nRate",
                            color = Color(0xFF4CAF50)
                        )
                        StatBox(
                            value = "8m",
                            label = "Avg\nResponse Time",
                            color = Color(0xFFFF9800)
                        )
                        StatBox(
                            value = "94",
                            label = "Trust\nScore",
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            // Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Helper Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Response Radius
                    SettingItem(
                        icon = Icons.Default.LocationOn,
                        title = "Response Radius",
                        subtitle = "How far you're willing to travel",
                        action = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (responseRadius > 1) {
                                            responseRadius--
                                            updateSetting("responseRadius", responseRadius)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, "Decrease")
                                }
                                Text(
                                    "$responseRadius km",
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        if (responseRadius < 20) {
                                            responseRadius++
                                            updateSetting("responseRadius", responseRadius)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, "Increase")
                                }
                            }
                        }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Alert Preferences
                    SettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Alert Preferences",
                        subtitle = "Choose which alerts to receive",
                        action = {
                            // Will be expanded in a dialog
                        }
                    )

                    Column(
                        modifier = Modifier.padding(start = 40.dp, top = 8.dp)
                    ) {
                        PreferenceToggle(
                            text = "High Risk Alerts",
                            isChecked = isReceiveHighRisk,
                            onCheckedChange = {
                                isReceiveHighRisk = it
                                updateSetting("receiveHighRisk", it)
                            }
                        )
                        PreferenceToggle(
                            text = "Medium Risk Alerts",
                            isChecked = isReceiveMediumRisk,
                            onCheckedChange = {
                                isReceiveMediumRisk = it
                                updateSetting("receiveMediumRisk", it)
                            }
                        )
                        PreferenceToggle(
                            text = "Low Risk Alerts",
                            isChecked = isReceiveLowRisk,
                            onCheckedChange = {
                                isReceiveLowRisk = it
                                updateSetting("receiveLowRisk", it)
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Active Hours
                    SettingItem(
                        icon = Icons.Default.Schedule,
                        title = "Active Hours",
                        subtitle = "When you're available to respond",
                        action = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(activeHoursStart, fontWeight = FontWeight.Bold)
                                Text(" to ", color = Color.Gray)
                                Text(activeHoursEnd, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Safety Guidelines
                    SettingItem(
                        icon = Icons.Default.Security,
                        title = "Safety Guidelines",
                        subtitle = "View helper safety protocols",
                        action = {
                            IconButton(onClick = { /* Open guidelines */ }) {
                                Icon(Icons.Default.OpenInNew, "Open")
                            }
                        }
                    )

                    // Male Helper Guidelines
                    if (userData?.gender == "male") {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingItem(
                            icon = Icons.Default.Info,
                            title = "Male Helper Guidelines",
                            subtitle = "Special guidelines for male helpers",
                            action = {
                                IconButton(onClick = { /* Open male guidelines */ }) {
                                    Icon(Icons.Default.OpenInNew, "Open")
                                }
                            }
                        )
                    }
                }
            }

            // Emergency Contacts
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            "Emergency Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { /* Edit contacts */ }) {
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EmergencyContactItem(
                        name = "Police Emergency",
                        number = "999",
                        type = "Emergency"
                    )
                    EmergencyContactItem(
                        name = "National Emergency",
                        number = "112",
                        type = "Emergency"
                    )
                    EmergencyContactItem(
                        name = "Women Helpline",
                        number = "109",
                        type = "Helpline"
                    )
                }
            }

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatBox(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            title,
            modifier = Modifier.padding(end = 16.dp),
            tint = Color(0xFF1976D2)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        action()
    }
}

@Composable
fun PreferenceToggle(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 14.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50)
            )
        )
    }
}

@Composable
fun EmergencyContactItem(name: String, number: String, type: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(name, fontWeight = FontWeight.Medium)
                Text(number, fontSize = 14.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(
                        when(type) {
                            "Emergency" -> Color(0xFFF44336)
                            "Helpline" -> Color(0xFF2196F3)
                            else -> Color(0xFF9C27B0)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    type,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { /* Dial number */ },
                modifier = Modifier.background(
                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                    CircleShape
                )
            ) {
                Icon(
                    Icons.Default.Phone,
                    "Call",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}