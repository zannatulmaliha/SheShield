package com.example.sheshield.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Delete
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
fun DataStorageScreen(onBack: () -> Unit) {
    var recordAudio by remember { mutableStateOf(true) }
    var recordVideo by remember { mutableStateOf(false) }
    var saveReports by remember { mutableStateOf(true) }
    var storageLocation by remember { mutableStateOf("Device") } // Device or Cloud

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Storage") },
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
                "Manage your data and storage",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                "Configure recording settings and storage preferences",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recording Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recording Settings", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Record Audio", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = recordAudio,
                            onCheckedChange = { recordAudio = it },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Record Video", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = recordVideo,
                            onCheckedChange = { recordVideo = it },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    if (recordVideo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Video recording requires more storage space",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Report Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "Reports",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Save Reports", fontWeight = FontWeight.Medium)
                                Text("Store incident reports", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = saveReports,
                            onCheckedChange = { saveReports = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2196F3),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFBDBDBD)
                            )
                        )
                    }

                    if (saveReports) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Reports include location, time, and incident details",
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            // Storage Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SdStorage,
                            contentDescription = "Storage",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Storage Location", fontWeight = FontWeight.Medium)
                            Text("Choose where data is stored", fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Storage options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { storageLocation = "Device" }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = storageLocation == "Device",
                                onClick = { storageLocation = "Device" }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Device Storage", fontWeight = FontWeight.Medium)
                                Text("Fast access, limited space", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { storageLocation = "Cloud" }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = storageLocation == "Cloud",
                                onClick = { storageLocation = "Cloud" }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cloud Storage", fontWeight = FontWeight.Medium)
                                Text("Backup enabled, requires internet", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Storage Usage Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Storage Usage", fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        Text("1.2 GB / 5 GB", fontSize = 14.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = 0.24f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF3B82F6),
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SOS Alerts", fontSize = 12.sp, color = Color.Gray)
                            Text("450 MB", fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text("Recordings", fontSize = 12.sp, color = Color.Gray)
                            Text("650 MB", fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text("Reports", fontSize = 12.sp, color = Color.Gray)
                            Text("100 MB", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Clear Data Button
            Button(
                onClick = { /* Clear data */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Data", fontWeight = FontWeight.Bold)
            }
        }
    }
}