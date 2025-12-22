package com.example.sheshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.*
import com.example.sheshield.screens.helper.HelperDashboard
import com.example.sheshield.screens.helper.HelperProfileScreen
import com.example.sheshield.ui.theme.SheShieldTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

import com.example.sheshield.screens.helper.HelperScreen

// ADD THIS IMPORT
import com.example.sheshield.screens.TrackRouteScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SheShieldTheme {
                SheShieldApp()
            }
        }
    }
}

enum class AppMode {
    USER, HELPER
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    CONTACTS("Contacts", Icons.Default.Person),
    MAP("Map", Icons.Default.LocationOn),
    AI("AI Help", Icons.Default.Face),
    PROFILE("Profile", Icons.Default.AccountBox),
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheShieldApp() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // State variables
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var appMode by rememberSaveable { mutableStateOf(AppMode.USER) }
    var userData by remember { mutableStateOf<UserData?>(null) }

    // Fetch user data when logged in
    LaunchedEffect(key1 = auth.currentUser) {
        isLoading = true
        delay(500)

        val currentUser = auth.currentUser
        isLoggedIn = currentUser != null

        if (isLoggedIn == true && currentUser != null) {
            // Fetch user data from Firestore
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.toObject(UserData::class.java)

                        // Ensure userId is set
                        userData = userData?.copy(userId = currentUser.uid)

                        // Set default mode based on user type
                        when (userData?.userType) {
                            "helper" -> appMode = AppMode.HELPER
                            "user" -> appMode = AppMode.USER
                            "user_helper" -> {
                                // Start in user mode, can switch to helper
                                appMode = AppMode.USER
                            }
                            else -> appMode = AppMode.USER
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    userData = null
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Show loading indicator
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    // Check login status
    else if (isLoggedIn == false) {
        LoginSignupScreen(
            onLoginSuccess = {
                isLoggedIn = true
                currentDestination = AppDestinations.HOME
            },
            onSwitchToHelperMode = {
                // This is for users who want to register as helpers from login
                // Not needed here, handled by userType selection
            }
        )
    }
    else if (isLoggedIn == true) {
        // Determine what to show based on user type
        when (userData?.userType) {
            "helper" -> {
                // Helper only - always show helper mode
                HelperModeApp(
                    onSwitchToUserMode = null, // No switching for helper-only users
                    onLogout = {
                        auth.signOut()
                        isLoggedIn = false
                        userData = null
                    },
                    userData = userData
                )
            }
            "user" -> {
                // User only - always show user mode
                UserModeApp(
                    currentDestination = currentDestination,
                    onDestinationChange = { currentDestination = it },
                    onLogout = {
                        auth.signOut()
                        isLoggedIn = false
                        userData = null
                    },
                    showSwitchToHelper = false // User-only can't switch
                )
            }
            "user_helper" -> {
                // User+Helper - can switch between modes
                if (appMode == AppMode.USER) {
                    UserModeApp(
                        currentDestination = currentDestination,
                        onDestinationChange = { currentDestination = it },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                            userData = null
                        },
                        showSwitchToHelper = true,
                        onSwitchToHelperMode = { appMode = AppMode.HELPER }
                    )
                } else {
                    HelperModeApp(
                        onSwitchToUserMode = { appMode = AppMode.USER },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                            userData = null
                        },
                        userData = userData
                    )
                }
            }
            else -> {
                // Default fallback
                UserModeApp(
                    currentDestination = currentDestination,
                    onDestinationChange = { currentDestination = it },
                    onLogout = {
                        auth.signOut()
                        isLoggedIn = false
                        userData = null
                    },
                    showSwitchToHelper = false
                )
            }
        }
    }
}

// Updated UserModeApp with optional switch button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserModeApp(
    currentDestination: AppDestinations,
    onDestinationChange: (AppDestinations) -> Unit,
    onLogout: () -> Unit,
    showSwitchToHelper: Boolean,
    onSwitchToHelperMode: (() -> Unit)? = null
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { onDestinationChange(destination) },
                        icon = { Icon(destination.icon, destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showSwitchToHelper && onSwitchToHelperMode != null) {
                FloatingActionButton(
                    onClick = onSwitchToHelperMode,
                    containerColor = Color(0xFF6200EE)
                ) {
                    Icon(
                        Icons.Default.SupervisorAccount,
                        "Switch to Helper Mode",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen()
                AppDestinations.CONTACTS -> TrustedContactsScreen(
                    onBack = { onDestinationChange(AppDestinations.HOME) }
                )
                AppDestinations.MAP -> TrackRouteScreen() // Updated Line
                AppDestinations.AI -> Text("AI Help Screen")
                AppDestinations.PROFILE -> ProfileScreen(
                    onBack = { onDestinationChange(AppDestinations.HOME) },
                    onLogout = onLogout
                )
            }
        }
    }
}

// Updated HelperModeApp - FIXED syntax errors
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperModeApp(
    onSwitchToUserMode: (() -> Unit)?,
    onLogout: () -> Unit,
    userData: UserData?
) {
    var currentScreen by rememberSaveable { mutableStateOf(HelperScreen.DASHBOARD) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Helper Mode")
                        if (userData?.gender == "male") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1976D2), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "MALE HELPER",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (onSwitchToUserMode != null) {
                        IconButton(
                            onClick = onSwitchToUserMode
                        ) {
                            Icon(Icons.Default.SwitchAccount, "Switch to User Mode")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1976D2) // Blue for helper mode
                )
            )
        },
        bottomBar = {
            NavigationBar {
                HelperScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                when(screen) {
                                    HelperScreen.DASHBOARD -> Icons.Default.Dashboard
                                    HelperScreen.ALERTS -> Icons.Default.Notifications
                                    HelperScreen.PROFILE -> Icons.Default.Person
                                    HelperScreen.SUPPORT -> Icons.Default.Help
                                },
                                screen.name
                            )
                        },
                        label = {
                            Text(
                                when(screen) {
                                    HelperScreen.DASHBOARD -> "Dashboard"
                                    HelperScreen.ALERTS -> "Alerts"
                                    HelperScreen.PROFILE -> "Profile"
                                    HelperScreen.SUPPORT -> "Help"
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                HelperScreen.DASHBOARD -> HelperDashboard(
                    onNavigate = { currentScreen = it },
                    onSwitchToUserMode = onSwitchToUserMode,
                    onAcceptAlert = { alert ->
                        println("Alert accepted: ${alert.id}")
                        currentScreen = HelperScreen.ALERTS
                    },
                    userData = userData
                )
                HelperScreen.ALERTS -> Text("Helper Alert Screen")
                HelperScreen.PROFILE -> HelperProfileScreen(
                    onBack = { currentScreen = HelperScreen.DASHBOARD },
                    onLogout = onLogout,
                    userData = userData
                )
                HelperScreen.SUPPORT -> Text("Helper Support Screen")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SheShieldAppPreview() {
    SheShieldTheme {
        SheShieldApp()
    }
}