package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale


// Country data class
data class Country(
    val code: String,
    val name: String,
    val flag: String,
    val dialCode: String
)

// List of common countries
val countries = listOf(
    Country("BD", "Bangladesh", "🇧🇩", "+880"),
    Country("US", "United States", "🇺🇸", "+1"),
    Country("GB", "United Kingdom", "🇬🇧", "+44"),
    Country("IN", "India", "🇮🇳", "+91"),
    Country("PK", "Pakistan", "🇵🇰", "+92"),
    Country("CA", "Canada", "🇨🇦", "+1"),
    Country("AU", "Australia", "🇦🇺", "+61"),
    Country("SA", "Saudi Arabia", "🇸🇦", "+966"),
    Country("AE", "UAE", "🇦🇪", "+971"),
    Country("MY", "Malaysia", "🇲🇾", "+60"),
    Country("SG", "Singapore", "🇸🇬", "+65"),
    Country("JP", "Japan", "🇯🇵", "+81"),
    Country("KR", "South Korea", "🇰🇷", "+82"),
    Country("CN", "China", "🇨🇳", "+86"),
    Country("RU", "Russia", "🇷🇺", "+7"),
    Country("DE", "Germany", "🇩🇪", "+49"),
    Country("FR", "France", "🇫🇷", "+33"),
    Country("IT", "Italy", "🇮🇹", "+39"),
    Country("ES", "Spain", "🇪🇸", "+34"),
    Country("BR", "Brazil", "🇧🇷", "+55"),
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
//    onNavigateToContacts: ()
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for personal information
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // State for dialogs
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }

    // Load user data from Firebase
    LaunchedEffect(key1 = auth.currentUser) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            userEmail = auth.currentUser?.email ?: "No email"

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userPhone = document.getString("phone") ?: ""
                        userAddress = document.getString("address") ?: ""
                        userName = document.getString("name") ?: "User"
                    } else {
                        val userData = hashMapOf(
                            "email" to userEmail,
                            "phone" to "",
                            "address" to "",
                            "name" to userEmail.substringBefore("@"),
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(userId).set(userData)
                        userPhone = ""
                        userAddress = ""
                        userName = userEmail.substringBefore("@")
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    userPhone = ""
                    userAddress = ""
                    userName = "User"
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Save data to Firebase function
    fun saveToFirebase(field: String, value: String) {
        val userId = auth.currentUser?.uid ?: return

        val updateData = hashMapOf<String, Any>(
            field to value,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .update(updateData)
            .addOnSuccessListener {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "✅ $field updated successfully!",
                        duration = SnackbarDuration.Short
                    )
                }
            }
            .addOnFailureListener { e ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "❌ Failed to update $field",
                        duration = SnackbarDuration.Short
                    )
                }
            }
    }

    // Dialogs
    if (showEmailDialog) {
        EditEmailDialog(
            currentEmail = userEmail,
            onDismiss = { showEmailDialog = false },
            onSave = { newEmail ->
                saveToFirebase("email", newEmail)
                userEmail = newEmail
                showEmailDialog = false
            }
        )
    }

    if (showPhoneDialog) {
        EnhancedPhoneDialog(
            currentPhone = userPhone,
            onDismiss = { showPhoneDialog = false },
            onSave = { newPhone ->
                saveToFirebase("phone", newPhone)
                userPhone = newPhone
                showPhoneDialog = false
            }
        )
    }

    if (showAddressDialog) {
        EnhancedAddressDialog(
            currentAddress = userAddress,
            onDismiss = { showAddressDialog = false },
            onSave = { newAddress ->
                saveToFirebase("address", newAddress)
                userAddress = newAddress
                showAddressDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(bottom = 35.dp)
            ) {
                profile_settings(userName = userName)

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6200EE))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading profile...", color = Color.Gray)
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(25.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("PERSONAL INFORMATION", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                        personal_info(
                            email = userEmail,
                            phone = if (userPhone.isEmpty()) "Tap to add phone" else formatPhoneForDisplay(userPhone),
                            address = if (userAddress.isEmpty()) "Tap to add address" else userAddress,
                            onEmailClick = { showEmailDialog = true },
                            onPhoneClick = { showPhoneDialog = true },
                            onAddressClick = { showAddressDialog = true }
                        )

                        Text("SAFETY SETTINGS", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        safety_settings()
                        Text("PRIVACY & SECURITY", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        privacy_security()
                        Text("SUPPORT & INFORMATION", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Support_info()
                        verified_users()
                    }
                }
            }

            // LOGOUT BUTTON
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Email", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter your new email address:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValidEmail(email) && email.isNotEmpty()
                )
                if (!isValidEmail(email) && email.isNotEmpty()) {
                    Text("Please enter a valid email", color = Color.Red, fontSize = 12.sp)
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidEmail(email)) {
                        isLoading = true
                        onSave(email)
                    }
                },
                enabled = isValidEmail(email) && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

// Enhanced Phone Dialog with Country Picker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPhoneDialog(
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(extractPhoneNumber(currentPhone)) }
    var selectedCountry by remember {
        mutableStateOf(
            countries.find {
                currentPhone.startsWith(it.dialCode) ||
                        it.code == getCountryCodeFromLocale()
            } ?: countries.find { it.code == "BD" } ?: countries[0]
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Phone Number", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // Country Picker
                Text("Select Country:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCountry.flag} ${selectedCountry.name} (${selectedCountry.dialCode})",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Country") }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(country.flag, modifier = Modifier.padding(end = 8.dp))
                                        Text("${country.name} (${country.dialCode})")
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

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number Input
                Text("Phone Number:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Country Code Display
                    Surface(
                        color = Color(0xFF6200EE).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(56.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                selectedCountry.dialCode,
                                color = Color(0xFF6200EE),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Phone Number Input
                    OutlinedTextField(
                        value = formatPhoneInput(phoneNumber),
                        onValueChange = { newValue ->
                            // Remove all non-digits
                            val digitsOnly = newValue.filter { it.isDigit() }
                            // Limit to reasonable length
                            phoneNumber = digitsOnly.take(15)
                            errorMessage = null
                        },
                        label = { Text("Phone Number") },
                        placeholder = { Text("Enter your phone number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = errorMessage != null
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                // Phone Number Format Example
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Example: ${selectedCountry.dialCode} 1712 345678",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullPhoneNumber = "${selectedCountry.dialCode}${phoneNumber}"

                    // Basic validation
                    when {
                        phoneNumber.isEmpty() -> {
                            errorMessage = "Please enter a phone number"
                        }
                        phoneNumber.length < 7 -> {
                            errorMessage = "Phone number is too short"
                        }
                        phoneNumber.length > 15 -> {
                            errorMessage = "Phone number is too long"
                        }
                        !phoneNumber.all { it.isDigit() } -> {
                            errorMessage = "Phone number should contain only digits"
                        }
                        else -> {
                            // Valid phone number
                            isLoading = true
                            onSave(fullPhoneNumber)
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
// Helper function to format phone number as user types
private fun formatPhoneInput(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.length <= 4 -> digits
        digits.length <= 7 -> "${digits.substring(0, 4)} ${digits.substring(4)}"
        else -> "${digits.substring(0, 4)} ${digits.substring(4, 7)} ${digits.substring(7).take(4)}"
    }
}
// Enhanced Address Dialog with Multiple Fields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddressDialog(
    currentAddress: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // Parse current address into components
    val addressParts = parseAddress(currentAddress)

    var houseNumber by remember { mutableStateOf(addressParts.houseNumber) }
    var street by remember { mutableStateOf(addressParts.street) }
    var city by remember { mutableStateOf(addressParts.city) }
    var state by remember { mutableStateOf(addressParts.state) }
    var zipCode by remember { mutableStateOf(addressParts.zipCode) }
    var country by remember { mutableStateOf(addressParts.country) }
    var additionalInfo by remember { mutableStateOf(addressParts.additionalInfo) }

    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Address", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // Address Form
                Text("Enter your address details:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row 1: House Number & Street
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = houseNumber,
                        onValueChange = { houseNumber = it },
                        label = { Text("House/Apt No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("123") }
                    )

                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        placeholder = { Text("Main Street") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: City & State
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Dhaka") }
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State/Province") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Dhaka Division") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Zip Code & Country
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = { Text("ZIP/Postal Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("1230") }
                    )

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Bangladesh") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Additional Info
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    label = { Text("Additional Info (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2,
                    placeholder = { Text("Landmark, floor, etc.") }
                )

                // Address Preview
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Address Preview:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatAddress(
                                houseNumber, street, city, state, zipCode, country, additionalInfo
                            ),
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    val formattedAddress = formatAddress(
                        houseNumber, street, city, state, zipCode, country, additionalInfo
                    )
                    onSave(formattedAddress)
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun personal_info(
    email: String,
    phone: String,
    address: String,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onAddressClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        // Email Row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable { onEmailClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF6200EE)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Email", fontWeight = FontWeight.Medium)
                Text(text = email, color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }

        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

        // Phone Row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable { onPhoneClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF6200EE)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Phone Number", fontWeight = FontWeight.Medium)
                Text(text = phone, color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }

        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

        // Address Row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable { onAddressClick() }
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "address",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF6200EE)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Home Address", fontWeight = FontWeight.Medium)
                Text(
                    text = address,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun profile_settings(userName: String = "User") {
    Surface(
        color = Color(0xFF6200EE),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 25.dp,
            bottomEnd = 25.dp
        ),
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(bottom = 10.dp)) {
            Text(
                "Profile & Settings",
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = Color(0xFF7C4DFF),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(1f),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF6200EE)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            userName,
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Member since November 2024",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// In ProfileScreen function
//safety_settings(onContactsClick = onNavigateToContacts) // <--- Pass the function here
@Composable
//fun safety_settings(onContactsClick = onNavigateToContacts){
fun safety_settings() {
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "SOS",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("SOS Triggers", fontWeight = FontWeight.Medium)
                Text(text = "Shake, voice, power button", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contacts",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Trusted Contacts", fontWeight = FontWeight.Medium)
                Text(text = "3 contacts added", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "notification",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF9C27B0)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Notifications", fontWeight = FontWeight.Medium)
                Text(text = "Push, SMS, email alerts", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun privacy_security() {
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF2196F3)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Privacy Settings", fontWeight = FontWeight.Medium)
                Text(text = "Control your data", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Data",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF2196F3)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Data & Storage", fontWeight = FontWeight.Medium)
                Text(text = "Recording, report, history", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "app lock",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF2196F3)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("App Lock", fontWeight = FontWeight.Medium)
                Text(text = "Require PIN or biometric", color = Color.Gray, fontSize = 14.sp)
            }
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }
}

@Composable
fun Support_info() {
    Column(
        modifier = Modifier
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Help",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFFFF9800)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Help Center", fontWeight = FontWeight.Medium)
                Text(text = "FAQs and guides", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = "Support",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFFFF9800)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Contact Support", fontWeight = FontWeight.Medium)
                Text(text = "Get help from our team", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "About",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFFFF9800)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("About SheShield", fontWeight = FontWeight.Medium)
                Text(text = "Version 1.0.0", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun verified_users() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE8F5E9))
            .border(
                width = 1.dp,
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified",
                modifier = Modifier.padding(10.dp),
                tint = Color(0xFF4CAF50)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Verified User", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text(
                    text = "Your account is verified with email and phone number",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
    }
}

// Helper Functions
private fun extractPhoneNumber(fullPhone: String): String {
    if (fullPhone.isEmpty()) return ""

    // Remove country code if present
    for (country in countries) {
        if (fullPhone.startsWith(country.dialCode)) {
            return fullPhone.substring(country.dialCode.length).trim()
        }
    }

    return fullPhone
}

private fun formatPhoneForDisplay(phone: String): String {
    if (phone.isEmpty()) return ""

    // Try to format the phone number
    for (country in countries) {
        if (phone.startsWith(country.dialCode)) {
            val number = phone.substring(country.dialCode.length)
            return "${country.flag} ${country.dialCode} ${formatPhoneNumber(number)}"
        }
    }

    return phone
}

private fun formatPhoneNumber(number: String): String {
    val cleaned = number.filter { it.isDigit() }

    return when (cleaned.length) {
        10 -> "${cleaned.substring(0, 4)} ${cleaned.substring(4, 7)} ${cleaned.substring(7)}"
        11 -> "${cleaned.substring(0, 4)} ${cleaned.substring(4, 7)} ${cleaned.substring(7)}"
        else -> cleaned.chunked(4).joinToString(" ")
    }
}

private fun getCountryCodeFromLocale(): String {
    return when (Locale.getDefault().country) {
        "US" -> "US"
        "GB" -> "GB"
        "BD" -> "BD"
        "IN" -> "IN"
        else -> "BD" // Default to Bangladesh
    }
}

// Address parsing and formatting
data class AddressParts(
    val houseNumber: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val additionalInfo: String
)

private fun parseAddress(address: String): AddressParts {
    if (address.isEmpty()) {
        return AddressParts("", "", "", "", "", "Bangladesh", "")
    }

    // Simple parsing - you can enhance this based on your needs
    val parts = address.split(",").map { it.trim() }

    return when (parts.size) {
        1 -> AddressParts("", parts[0], "", "", "", "Bangladesh", "")
        2 -> AddressParts("", parts[0], parts[1], "", "", "Bangladesh", "")
        3 -> AddressParts("", parts[0], parts[1], parts[2], "", "Bangladesh", "")
        else -> {
            val country = parts.lastOrNull()?.takeIf {
                it.length > 2 && !it.matches(Regex(".*\\d.*"))
            } ?: "Bangladesh"

            AddressParts(
                houseNumber = parts.getOrElse(0) { "" },
                street = parts.getOrElse(1) { "" },
                city = parts.getOrElse(2) { "" },
                state = parts.getOrElse(3) { "" },
                zipCode = parts.find { it.matches(Regex("\\d+")) } ?: "",
                country = country,
                additionalInfo = parts.getOrElse(parts.size - 2) { "" }
            )
        }
    }
}

private fun formatAddress(
    houseNumber: String,
    street: String,
    city: String,
    state: String,
    zipCode: String,
    country: String,
    additionalInfo: String
): String {
    val addressParts = mutableListOf<String>()

    if (houseNumber.isNotEmpty()) addressParts.add(houseNumber)
    if (street.isNotEmpty()) addressParts.add(street)
    if (additionalInfo.isNotEmpty()) addressParts.add(additionalInfo)
    if (city.isNotEmpty()) addressParts.add(city)
    if (state.isNotEmpty()) addressParts.add(state)
    if (zipCode.isNotEmpty()) addressParts.add(zipCode)
    if (country.isNotEmpty()) addressParts.add(country)

    return addressParts.joinToString(", ")
}

// Helper function for email validation
private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}