package com.example.sheshield

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.*
import com.example.sheshield.screens.helper.HelperDashboard
import com.example.sheshield.screens.helper.HelperProfileScreen
import com.example.sheshield.screens.helper.HelperAlertsScreen // Make sure this is imported
import com.example.sheshield.screens.helper.HelperScreen // Make sure this is imported
import com.example.sheshield.ui.theme.SheShieldTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

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
                        userData = userData?.copy(userId = currentUser.uid)

                        // Set default mode based on user type
                        when (userData?.userType) {
                            "helper" -> appMode = AppMode.HELPER
                            "user" -> appMode = AppMode.USER
                            "user_helper" -> appMode = AppMode.USER // Default to user, can switch
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
            onSwitchToHelperMode = {}
        )
    }
    else if (isLoggedIn == true) {
        // Determine what to show based on user type
        when (userData?.userType) {
            "helper" -> {
                HelperModeApp(
                    onSwitchToUserMode = null,
                    onLogout = {
                        auth.signOut()
                        isLoggedIn = false
                        userData = null
                    },
                    userData = userData
                )
            }
            "user" -> {
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
            "user_helper" -> {
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
                AppDestinations.MAP -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Map Screen") }
                AppDestinations.AI -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("AI Help Screen") }
                AppDestinations.PROFILE -> ProfileScreen(
                    onBack = { onDestinationChange(AppDestinations.HOME) },
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperModeApp(
    onSwitchToUserMode: (() -> Unit)?,
    onLogout: () -> Unit,
    userData: UserData?
) {
    val context = LocalContext.current
    // Default start screen
    var currentScreen by rememberSaveable { mutableStateOf(HelperScreen.DASHBOARD) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Helper Mode", color = Color.White, fontWeight = FontWeight.Bold)
                        if (userData?.gender == "male") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "MALE HELPER",
                                    color = Color(0xFF1976D2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (onSwitchToUserMode != null) {
                        IconButton(onClick = onSwitchToUserMode) {
                            Icon(Icons.Default.SwitchAccount, "Switch to User Mode", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1976D2)
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
                            // FIXED: Added HISTORY case to be exhaustive
                            Icon(
                                when(screen) {
                                    HelperScreen.DASHBOARD -> Icons.Default.Dashboard
                                    HelperScreen.ALERTS -> Icons.Default.Notifications
                                    HelperScreen.PROFILE -> Icons.Default.Person
                                    HelperScreen.SUPPORT -> Icons.Default.Help
                                    HelperScreen.HISTORY -> Icons.Default.History
                                },
                                screen.name
                            )
                        },
                        label = {
                            // FIXED: Added HISTORY case to be exhaustive
                            Text(
                                when(screen) {
                                    HelperScreen.DASHBOARD -> "Dashboard"
                                    HelperScreen.ALERTS -> "Alerts"
                                    HelperScreen.PROFILE -> "Profile"
                                    HelperScreen.SUPPORT -> "Help"
                                    HelperScreen.HISTORY -> "History"
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // FIXED: Added HISTORY case to be exhaustive
            when (currentScreen) {
                HelperScreen.DASHBOARD -> HelperDashboard(
                    onNavigate = { screen: HelperScreen ->
                        currentScreen = screen
                    },
                    onSwitchToUserMode = onSwitchToUserMode,
                    onAcceptAlert = { alert ->
                        Toast.makeText(context, "Accepted alert: ${alert.userName}", Toast.LENGTH_SHORT).show()
                        currentScreen = HelperScreen.ALERTS
                    },
                    userData = userData
                )
                HelperScreen.ALERTS -> HelperAlertsScreen(
                    onBack = { currentScreen = HelperScreen.DASHBOARD },
                    onNavigateToMap = {
                        Toast.makeText(context, "Map Navigation", Toast.LENGTH_SHORT).show()
                    }
                )
                HelperScreen.PROFILE -> HelperProfileScreen(
                    onBack = { currentScreen = HelperScreen.DASHBOARD },
                    onLogout = onLogout,
                    userData = userData
                )
                HelperScreen.SUPPORT -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Support Screen Placeholder")
                }
                HelperScreen.HISTORY -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("History Screen Placeholder")
                }
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