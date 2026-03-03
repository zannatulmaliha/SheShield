package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

// --- Midnight Theme Palette ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentAmber = Color(0xFFF59E0B)
private val DangerRose = Color(0xFFFB7185)

data class Country(
    val code: String, val name: String, val flag: String, val dialCode: String
)

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
    onNavigateToContacts: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToDataStorage: () -> Unit,
    onNavigateToHelpCenter: () -> Unit,
    onNavigateToContactSupport: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isAppLockEnabled by remember { mutableStateOf(true) }

    var showEmailDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }

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
                        isAppLockEnabled = document.getBoolean("appLock") ?: true
                    } else {
                        val userData = hashMapOf(
                            "email" to userEmail, "phone" to "", "address" to "",
                            "name" to userEmail.substringBefore("@"), "appLock" to true,
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(userId).set(userData)
                        userName = userEmail.substringBefore("@")
                    }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        }
    }

    fun saveToFirebase(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).update(hashMapOf(field to value, "updatedAt" to System.currentTimeMillis()))
            .addOnSuccessListener { scope.launch { snackbarHostState.showSnackbar("✅ Settings Synced") } }
    }

    // Dialog Triggers
    if (showEmailDialog) EditEmailDialog(userEmail, { showEmailDialog = false }, { userEmail = it; saveToFirebase("email", it) })
    if (showPhoneDialog) EnhancedPhoneDialog(userPhone, { showPhoneDialog = false }, { userPhone = it; saveToFirebase("phone", it) })
    if (showAddressDialog) EnhancedAddressDialog(userAddress, { showAddressDialog = false }, { userAddress = it; saveToFirebase("address", it) })

    Box(modifier = Modifier.fillMaxSize().background(MidnightBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {

                ProfileHeader(userName)

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                } else {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        SectionTitle("ACCOUNT DETAILS")
                        PersonalCard(userEmail, userPhone, userAddress, {showEmailDialog=true}, {showPhoneDialog=true}, {showAddressDialog=true})

                       // SectionTitle("PROTECTION")
                    //  SafetyCard(onSOSClick, onNavigateToContacts, onNavigateToNotifications)

                        SectionTitle("SECURITY")
                        SecurityCard(onNavigateToPrivacy, onNavigateToDataStorage, isAppLockEnabled) {
                            isAppLockEnabled = it; saveToFirebase("appLock", it)
                        }

                        SectionTitle("ASSISTANCE")
                        SupportCard(onHelpCenterClick = onNavigateToHelpCenter, onContactSupportClick = onNavigateToContactSupport, onAboutClick = onNavigateToAbout)

                        VerifiedBanner()

                        LogoutButton(onLogout)
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
    }
}

@Composable
fun ProfileHeader(userName: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase)))
        .padding(top = 48.dp, bottom = 24.dp)) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Profile", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(20.dp))
            GlassCard {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(64.dp)
                        .background(AccentPurple.copy(0.1f), CircleShape)
                        .border(1.dp, AccentPurple.copy(0.4f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Shield Member since 2024", color = Color.White.copy(0.5f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalCard(email: String, phone: String, address: String, onEmailClick: () -> Unit, onPhoneClick: () -> Unit, onAddressClick: () -> Unit) {
    GlassCard {
        SettingsRow(Icons.Default.Email, "Email", email, AccentPurple, onEmailClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.Phone, "Phone", if(phone.isEmpty()) "Add Phone" else phone, AccentPurple, onPhoneClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.LocationOn, "Home", if(address.isEmpty()) "Add Address" else address, AccentPurple, onAddressClick)
    }
}

// FIXED: Parameters renamed to match your ProfileScreen call exactly
@Composable
fun SafetyCard(
    onSOSClick: () -> Unit,
    onContactsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    GlassCard {
        SettingsRow(Icons.Default.Warning, "SOS Triggers", "Voice & Shake", Color(0xFFD8B4FE), onSOSClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.Person, "Trusted Contacts", "Manage Circle", Color(0xFFD8B4FE), onContactsClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.Notifications, "Alerts", "SMS & Push", Color(0xFFD8B4FE), onNotificationsClick)
    }
}

@Composable
fun SecurityCard(onPrivacyClick: () -> Unit, onDataStorageClick: () -> Unit, isAppLockEnabled: Boolean, onAppLockToggle: (Boolean) -> Unit) {
    GlassCard {
        SettingsRow(Icons.Default.Lock, "Privacy", "Data control", AccentBlue, onPrivacyClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.Check, "Data & Storage", "Report history", AccentBlue, onDataStorageClick)
        Divider(color = GlassBorder)
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, null, tint = AccentBlue, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("App Lock", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Biometric security", color = Color.White.copy(0.5f), fontSize = 13.sp)
            }
            Switch(
                checked = isAppLockEnabled,
                onCheckedChange = onAppLockToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentEmerald)
            )
        }
    }
}

@Composable
fun SupportCard(onHelpCenterClick: () -> Unit, onContactSupportClick: () -> Unit, onAboutClick: () -> Unit) {
    GlassCard {
        SettingsRow(Icons.Default.Info, "Help Center", "FAQs", AccentAmber, onHelpCenterClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.MailOutline, "Support", "Contact Us", AccentAmber, onContactSupportClick)
        Divider(color = GlassBorder)
        SettingsRow(Icons.Default.Favorite, "About", "Version 1.0.0", AccentAmber, onAboutClick)
    }
}

@Composable
fun VerifiedBanner() {
    Surface(
        color = AccentEmerald.copy(0.08f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, AccentEmerald.copy(0.3f), RoundedCornerShape(20.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, null, tint = AccentEmerald)
            Spacer(Modifier.width(12.dp))
            Text("Verified Shield Account", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DangerRose.copy(0.15f), contentColor = DangerRose),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRose.copy(0.3f))
    ) {
        Text("LOGOUT SESSION", fontWeight = FontWeight.Black)
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, tint: Color, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(sub, color = Color.White.copy(0.5f), fontSize = 13.sp, maxLines = 1)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White.copy(0.2f))
    }
}

@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = GlassWhite,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        content = { Column(content = content) }
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White.copy(0.3f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

private fun extractPhoneNumber(fullPhone: String): String {
    if (fullPhone.isEmpty()) return ""
    for (country in countries) {
        if (fullPhone.startsWith(country.dialCode)) return fullPhone.substring(country.dialCode.length).trim()
    }
    return fullPhone
}

private fun formatPhoneForDisplay(phone: String): String {
    if (phone.isEmpty()) return ""
    for (country in countries) {
        if (phone.startsWith(country.dialCode)) {
            val num = phone.substring(country.dialCode.length)
            return "${country.flag} ${country.dialCode} ${num.chunked(4).joinToString(" ")}"
        }
    }
    return phone
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Reuse your parseAddress and formatAddress logic from your provided code here...
// [Your parseAddress code block]
// [Your formatAddress code block]
// [Your Dialog implementations (EditEmailDialog, EnhancedPhoneDialog, etc.)]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1B4B), // Deep Indigo
        title = { Text("Update Email", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color.White.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = GlassBorder
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (isValidEmail(email)) onSave(email) }) {
                Text("SAVE", color = AccentPurple, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(0.5f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPhoneDialog(
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(extractPhoneNumber(currentPhone)) }
    var selectedCountry by remember {
        mutableStateOf(countries.find { currentPhone.startsWith(it.dialCode) } ?: countries[0])
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1B4B),
        title = { Text("Phone Number", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Simplified Country Display for Midnight UI
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite, RoundedCornerShape(8.dp))
                    .padding(12.dp)) {
                    Text("${selectedCountry.flag} ${selectedCountry.name} (${selectedCountry.dialCode})", color = Color.White)
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.all { char -> char.isDigit() }) phoneNumber = it },
                    label = { Text("Number", color = Color.White.copy(0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = GlassBorder
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave("${selectedCountry.dialCode}$phoneNumber") }) {
                Text("UPDATE", color = AccentPurple, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(0.5f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddressDialog(
    currentAddress: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val addressParts = parseAddress(currentAddress)
    var street by remember { mutableStateOf(addressParts.street) }
    var city by remember { mutableStateOf(addressParts.city) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1B4B),
        title = { Text("Home Address", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street", color = Color.White.copy(0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentPurple, unfocusedBorderColor = GlassBorder)
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City", color = Color.White.copy(0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentPurple, unfocusedBorderColor = GlassBorder)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave("$street, $city") }) {
                Text("SAVE", color = AccentPurple, fontWeight = FontWeight.Black)
            }
        }
    )
}

// Final Logic Helpers for Address Parsing
data class AddressParts(val street: String, val city: String)

private fun parseAddress(address: String): AddressParts {
    val parts = address.split(",")
    return AddressParts(
        street = parts.getOrNull(0)?.trim() ?: "",
        city = parts.getOrNull(1)?.trim() ?: ""
    )
}