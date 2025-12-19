
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

// Define the color locally
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
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
                            color = Color(0xFF1F2937)
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
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple600)
                }
            }
        }
    }
}

//@Composable
//fun ContactCard(
//    contact: com.example.sheshield.models.Contact,
//    onDelete: () -> Unit,
//    onSendTest: () -> Unit,
//    onCall: () -> Unit
//) {
//    val context = LocalContext.current
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = contact.name,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF1F2937)
//                        )
//                    }
//                }
//                //delete b
//                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
//                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//            ContactInfoRow(icon = Icons.Default.Phone, text = contact.phone)
//        }
//        // CALL BUTTON at Bottom Right
//
//        IconButton(
//            onClick = {
//                // SIMPLE: Just make the call
//                val intent = Intent(Intent.ACTION_CALL).apply {
//                    data = Uri.parse("tel:${contact.phone}")
//                }
//                context.startActivity(intent)
//            },
////            onClick = onCall,
//            modifier = Modifier
//
////                .fillMaxWidth()  // Take full width
//                .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)  // Keep your padding
//                .size(48.dp),
//            colors = IconButtonDefaults.iconButtonColors(
//                containerColor = Color(0xFFDC2626).copy(alpha = 0.9f),
//                contentColor = Color.White
//            )
//        ) {
//            Icon(Icons.Default.Call, contentDescription = "Emergency Call", modifier = Modifier.size(24.dp))
//        }
//    }
//}

//@Composable
//fun ContactCard(
//    contact: com.example.sheshield.models.Contact,
//    onDelete: () -> Unit,
//    onSendTest: () -> Unit
//) {
//    val context = LocalContext.current
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = contact.name,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF1F2937)
//                        )
//                    }
//                }
//                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
//                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//            ContactInfoRow(icon = Icons.Default.Phone, text = contact.phone)
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Emergency Call Button - FIXED VERSION
//            Button(
//                onClick = {
//                    // Use the helper
//                    CallPermissionHelper.makeCall(context, contact.phone)
//                },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFDC2626),
//                    contentColor = Color.White
//                ),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Icon(Icons.Default.Call, contentDescription = "Emergency Call", modifier = Modifier.size(20.dp))
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Emergency Call")
//            }
//        }
//    }
//}

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
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
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Emergency Call", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Call")
            }
        }
    }
}



@Composable
fun ContactInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
        Text(text = text, fontSize = 14.sp, color = Color(0xFF4B5563))
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
//    val isFormValid = name.isNotEmpty() && phoneNumber.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Trusted Contact", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))


// Container with border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
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
                                        color = Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp,
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
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Dropdown Menu
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .width(250.dp) // Fixed width for dropdown
                                    .height(300.dp)
                            ) {
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(country.flag, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(country.dialCode, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(country.name, color = Color.Gray, fontSize = 14.sp)
                                            }
                                        },
                                        onClick = {
                                            selectedCountry = country
                                            expanded = false
                                        }
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
                                color = Color(0xFF374151),
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
                                    color = Color.Black
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (phoneNumber.isEmpty()) {
                                            Text(
                                                "Enter phone number",
                                                color = Color(0xFF9CA3AF),
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
                Text("Full number: $fullPhoneNumber", fontSize = 12.sp, color = Purple600, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            val isFormValid = name.trim().isNotEmpty() && phoneNumber.trim().isNotEmpty()
            // Action Buttons
            // Action Buttons - Minimal version
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onAdd(name, fullPhoneNumber) },
                    enabled = isFormValid
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
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E40AF))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 14.sp, color = Color(0xFF1E3A8A), lineHeight = 20.sp)
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
            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Trusted Helper Badge", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                Text(text = "Verify your ID to become a helper for others in your community.", fontSize = 13.sp, color = Color(0xFF064E3B))
            }
        }
    }
}