package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Security
// Define the color locally
val Purple600 = Color(0xFF6200EE)

// Define the data classes needed for this screen
data class Contact(
    val id: Int,
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String,
    val verified: Boolean
)

val sampleContacts = listOf(
    Contact(1, "Sarah Mom", "Mother", "+1 234-567-8901", "sarah@email.com", true),
    Contact(2, "John Dad", "Father", "+1 234-567-8902", "john@email.com", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(onBack: () -> Unit) {
    var contacts by remember { mutableStateOf(sampleContacts) }
    var showAddForm by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactRelationship by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactEmail by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trusted Contacts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Purple600,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Trusted Contacts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${contacts.size} contact${if (contacts.size != 1) "s" else ""} • They'll receive your SOS alerts",
                        fontSize = 14.sp,
                        color = Color(0xFFD8B4FE)
                    )
                }
            }

            // Info Banner
            item {
                InfoCard(
                    icon = Icons.Default.Phone,
                    title = "How it works",
                    description = "When you activate SOS, these contacts will instantly receive your location, emergency details, and live tracking updates.",
                    backgroundColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFFBFDBFE)
                )
            }

            // Add Contact Button
            item {
                FilledTonalButton(
                    onClick = { showAddForm = !showAddForm },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Purple600,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Trusted Contact")
                }
            }

            // Add Contact Form
            if (showAddForm) {
                item {
                    AddContactForm(
                        name = newContactName,
                        onNameChange = { newContactName = it },
                        relationship = newContactRelationship,
                        onRelationshipChange = { newContactRelationship = it },
                        phone = newContactPhone,
                        onPhoneChange = { newContactPhone = it },
                        email = newContactEmail,
                        onEmailChange = { newContactEmail = it },
                        onAdd = {
                            val newContact = Contact(
                                id = (contacts.maxOfOrNull { it.id } ?: 0) + 1,
                                name = newContactName,
                                relationship = newContactRelationship,
                                phone = newContactPhone,
                                email = newContactEmail,
                                verified = false
                            )
                            contacts = contacts + newContact
                            newContactName = ""
                            newContactRelationship = ""
                            newContactPhone = ""
                            newContactEmail = ""
                            showAddForm = false
                        },
                        onCancel = { showAddForm = false }
                    )
                }
            }

            // Contact List
            items(contacts, key = { it.id }) { contact ->
                ContactCard(
                    contact = contact,
                    onDelete = {
                        contacts = contacts.filter { it.id != contact.id }
                    },
                    onSendTest = {
                        // Handle send test alert
                    },
                    onCall = {
                        // Handle phone call
                    }
                )
            }

            // Verified Helpers Section
            item {
                Column(modifier = Modifier.padding(vertical = 24.dp)) {
                    Text(
                        text = "Become a Verified Helper",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    VerifiedHelperCard()
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    onDelete: () -> Unit,
    onSendTest: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (contact.verified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Pending",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = contact.relationship,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Info
            ContactInfoRow(
                icon = Icons.Default.Phone,
                text = contact.phone
            )
            Spacer(modifier = Modifier.height(8.dp))
            ContactInfoRow(
                icon = Icons.Default.Email,
                text = contact.email
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Verification Status
            if (!contact.verified) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFFFDE68A),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Verification pending - Contact needs to confirm via SMS",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            if (contact.verified) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSendTest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFF3E8FF),
                            contentColor = Color(0xFF7C3AED)
                        )
                    ) {
                        Text("Send Test Alert")
                    }
//                    OutlinedButton(
//                    FilledTonalButton(
//                        onClick = onCall,
//                        modifier = Modifier.size(52.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = ButtonDefaults.filledTonalButtonColors(
////                            contentColor = Color(0xFF6B7280)
//                            containerColor = Color(0xFFE5E7EB),  // Light gray background
//                            contentColor = Color(0xFF374151)
//                        )
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Phone,
//                            contentDescription = "Call",
//                            modifier = Modifier.size(50.dp)
//                        )
//                    }
                    // Phone Call Button - USE BOX INSTEAD
                    Box(
                        modifier = Modifier
                            .size(56.dp)  // Total size
                            .background(Color(0xFFF3F4F6),
                                RoundedCornerShape(8.dp)),

//                            .clickable(onClick = onCall),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(32.dp)  // Icon size - can be large
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF4B5563)
        )
    }
}

@Composable
fun AddContactForm(
    name: String,
    onNameChange: (String) -> Unit,
    relationship: String,
    onRelationshipChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Add New Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = relationship,
                onValueChange = onRelationshipChange,
                label = { Text("Relationship (e.g., Mom)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAdd,
                    enabled = name.isNotEmpty() && phone.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple600)
                ) {
                    Text("Add Contact")
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    backgroundColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E40AF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF1E3A8A),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun VerifiedHelperCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Trusted Helper Badge",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
                Text(
                    text = "Verify your ID to become a helper for others in your community.",
                    fontSize = 13.sp,
                    color = Color(0xFF064E3B)
                )
            }
        }
    }
}
