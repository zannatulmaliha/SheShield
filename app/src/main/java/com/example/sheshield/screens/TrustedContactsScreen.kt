package com.example.sheshield.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.models.countries
import com.example.sheshield.viewmodel.ContactsViewModel

// --- GLASS GLOW PALETTE ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val AccentRed = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(onBack: () -> Unit) {
    val viewModel: ContactsViewModel = viewModel()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MidnightBase)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- GLASS TOP BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase.copy(alpha = 0.8f))))
                    .padding(top = 45.dp, bottom = 25.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(GlassWhite, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Trusted Contacts", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("${contacts.size} Guardians Active", color = AccentEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
            ) {
                // Info Banner
                item {
                    InfoCard(
                        icon = Icons.Default.Phone,
                        title = "How it works",
                        description = "When you activate SOS, these contacts will instantly receive your location and live tracking updates.",
                        accentColor = AccentPurple
                    )
                }

                // Add Contact Button
                item {
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showAddForm) "Cancel" else "Add Trusted Contact", fontWeight = FontWeight.Bold)
                    }
                }

                // Add Contact Form
                if (showAddForm) {
                    item {
                        AddContactForm(
                            name = newContactName,
                            onNameChange = { newContactName = it },
                            onAdd = { name, fullPhoneNumber ->
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

                // Error Message
                if (errorMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AccentRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, AccentRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = errorMessage ?: "", color = AccentRed, fontSize = 14.sp)
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

                // Community Section
                item {
                    Text("Community Network", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    VerifiedHelperCard()
                }
            }
        }

        // Loading Indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
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
    fun makeDirectCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply { data = Uri.parse("tel:$phoneNumber") }
        context.startActivity(intent)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { if (it) makeDirectCall(contact.phone) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite, RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(AccentPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(contact.phone, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = AccentRed.copy(alpha = 0.7f))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        makeDirectCall(contact.phone)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Call, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Emergency Call", fontWeight = FontWeight.Bold)
            }
        }
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
    val isFormValid = name.trim().isNotEmpty() && phoneNumber.trim().isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite, RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Text("New Guardian", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name", color = Color.White.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentPurple, unfocusedBorderColor = GlassBorder,
                    cursorColor = AccentPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Phone Row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Country Picker
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .weight(0.3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassWhite)
                        .clickable { expanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCountry.flag, fontSize = 20.sp)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(TopBarDeep).height(300.dp)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${country.flag} ${country.dialCode}", color = Color.White) },
                                onClick = { selectedCountry = country; expanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // BasicTextField for Phone
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .weight(0.7f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassWhite)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCountry.dialCode, color = AccentPurple, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            cursorBrush = Brush.verticalGradient(listOf(AccentPurple, AccentPurple)),
                            decorationBox = { inner ->
                                if (phoneNumber.isEmpty()) Text("Number", color = Color.White.copy(0.3f))
                                inner()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel", color = Color.White.copy(0.6f)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onAdd(name, fullPhoneNumber) },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, disabledContainerColor = GlassWhite)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, description: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row {
            Icon(icon, null, tint = accentColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = accentColor, fontWeight = FontWeight.Bold)
                Text(description, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun VerifiedHelperCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentEmerald.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, AccentEmerald.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ThumbUp, null, tint = AccentEmerald, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Trusted Helper Badge", color = AccentEmerald, fontWeight = FontWeight.Bold)
                Text("Protect others in your community.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}