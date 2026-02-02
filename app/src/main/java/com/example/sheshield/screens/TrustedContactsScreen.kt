package com.example.sheshield.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sheshield.viewmodel.ContactsViewModel
import com.example.sheshield.models.countries
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
// Add these imports at the top of your file
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch


import android.Manifest

// Dark theme colors matching HomeScreen
private val BackgroundDark = Color(0xFF1A1A2E)
private val SurfaceCard = Color(0xFF25254A)
private val PrimaryPurple = Color(0xFF8B7FFF)
private val SecondaryPurple = Color(0xFF6B5FEE)
private val AccentEmerald = Color(0xFF34D399)
private val AccentAmber = Color(0xFFFBBF24)
private val DangerRose = Color(0xFFFB7185)
private val TextPrimary = Color(0xFFE8E8F0)
private val TextSecondary = Color(0xFFB4B4C8)

// Keep original purple for compatibility
val Purple600 = Color(0xFF6200EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(onBack: () -> Unit) {
    // Get ViewModel
    val viewModel: ContactsViewModel = viewModel()

    // Observe state from ViewModel
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Local UI state
    var showAddForm by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                        ambientColor = PrimaryPurple.copy(alpha = 0.5f),
                        spotColor = PrimaryPurple.copy(alpha = 0.5f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4C3F8F),
                                    Color(0xFF3A2F6F)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryPurple.copy(alpha = 0.6f),
                                    PrimaryPurple.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                ) {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    "Trusted Contacts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
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
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BackgroundDark,
                                Color(0xFF16213E),
                                BackgroundDark
                            )
                        )
                    )
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Info
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryPurple.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                PrimaryPurple.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${contacts.size} Trusted Contact${if (contacts.size != 1) "s" else ""}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "They'll receive your SOS alerts",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Info Banner
                item {
                    InfoCard(
                        icon = Icons.Default.Phone,
                        title = "How it works",
                        description = "When you activate SOS, these contacts will instantly receive your location, emergency details, and live tracking updates.",
                        backgroundColor = PrimaryPurple.copy(alpha = 0.15f),
                        borderColor = PrimaryPurple.copy(alpha = 0.3f)
                    )
                }

                // Add Contact Button
                item {
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Trusted Contact", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Add Contact Form
                if (showAddForm) {
                    item {
                        AddContactForm(
                            name = newContactName,
                            onNameChange = { newContactName = it },
                            onAdd = { name, fullPhoneNumber ->
                                Log.d("TrustedScreen", "Form submitted: name='$name', phone='$fullPhoneNumber'")
                                viewModel.addContact(name, fullPhoneNumber)
                                newContactName = ""
                                showAddForm = false
                            },
                            onCancel = {
                                showAddForm = false
                                newContactName = ""
                            }
                        )
                    }
                }

                // After the AddContactForm item
                if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DangerRose
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                // Contact List
                items(contacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onDelete = { viewModel.deleteContact(contact) },
                        onSendTest = { /* TODO */ }

                    )
                }

                // Verified Helpers Section
                item {
                    Column(modifier = Modifier.padding(vertical = 24.dp)) {
                        Text(
                            text = "Become a Verified Helper",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        VerifiedHelperCard()
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple, strokeWidth = 3.dp)
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: com.example.sheshield.models.Contact,
    onDelete: () -> Unit,
    onSendTest: () -> Unit
) {
    val context = LocalContext.current

    // Helper function
    fun makeDirectCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            makeDirectCall(contact.phone)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            AccentEmerald.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRose)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ContactInfoRow(icon = Icons.Default.Phone, text = contact.phone)

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency Call Button
            Button(
                onClick = {
                    // Check permission
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        makeDirectCall(contact.phone)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRose,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Emergency Call", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Call", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}



@Composable
fun ContactInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentEmerald,
            modifier = Modifier.size(18.dp)
        )
        Text(text = text, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactForm(
    name: String,
    onNameChange: (String) -> Unit,
    onAdd: (name: String, fullPhoneNumber: String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(countries.find { it.code == "BD" } ?: countries[0]) }
    var phoneNumber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumber"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Add Trusted Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = PrimaryPurple) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Phone Number",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Container with border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Picker with ExposedDropdownMenuBox
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.2f)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Clickable country selector
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = PrimaryPurple.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            bottomStart = 12.dp,
                                            topEnd = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                    )
                                    .menuAnchor(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(selectedCountry.flag, fontSize = 18.sp)
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select country",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Dropdown Menu
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .width(250.dp)
                                    .height(300.dp)
                                    .background(SurfaceCard)
                            ) {
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(country.flag, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(country.dialCode, fontWeight = FontWeight.Medium, color = TextPrimary)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(country.name, color = TextSecondary, fontSize = 14.sp)
                                            }
                                        },
                                        onClick = {
                                            selectedCountry = country
                                            expanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Phone Input Area
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.8f)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Country code
                            Text(
                                selectedCountry.dialCode,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )

                            // Phone number input
                            BasicTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (phoneNumber.isEmpty()) {
                                            Text(
                                                "Enter phone number",
                                                color = TextSecondary,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            // Preview
            if (phoneNumber.isNotEmpty()) {
                Text(
                    "Full number: $fullPhoneNumber",
                    fontSize = 12.sp,
                    color = AccentEmerald,
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            val isFormValid = name.trim().isNotEmpty() && phoneNumber.trim().isNotEmpty()
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onAdd(name, fullPhoneNumber) },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentEmerald,
                        disabledContainerColor = AccentEmerald.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Contact", fontWeight = FontWeight.SemiBold)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryPurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun VerifiedHelperCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentEmerald.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Trusted Helper Badge",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Text(
                    text = "Verify your ID to become a helper for others in your community.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}