package com.example.sheshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.SOS.SosViewModel
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.*
import com.example.sheshield.screens.helper.HelperAlertsScreen
import com.example.sheshield.screens.helper.HelperScreen
import com.example.sheshield.screens.helper.HelperDashboard
import com.example.sheshield.screens.helper.HelperProfileScreen
import com.example.sheshield.screens.helper.HelperSupportScreen // ADDED: Import for Support Screen
import com.example.sheshield.ui.theme.SheShieldTheme
import com.example.sheshield.viewmodel.MovementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import com.example.sheshield.screens.AiHelpScreen

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

// Update AppDestinations enum to include all new screens
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    CONTACTS("Contacts", Icons.Default.Person),
    MAP("Map", Icons.Default.LocationOn),
    AI("AI Help", Icons.Default.Face),
    PROFILE("Profile", Icons.Default.AccountBox),
    SOS_SETTINGS("SOS", Icons.Default.Warning),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    PRIVACY("Privacy", Icons.Default.Lock),
    DATA_STORAGE("Data", Icons.Default.Storage),
    HELP_CENTER("Help", Icons.Default.Help),
    CONTACT_SUPPORT("Support", Icons.Default.Mail),
    ABOUT("About", Icons.Default.Info),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheShieldApp() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // ViewModels
    val movementViewModel: MovementViewModel = viewModel()
    val sosViewModel: SosViewModel = viewModel() // Initialize SOS ViewModel here

    // State variables
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var appMode by rememberSaveable { mutableStateOf(AppMode.USER) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var showMovementScreen by rememberSaveable { mutableStateOf(false) }

    // Initialize movement view model
    LaunchedEffect(Unit) {
        movementViewModel.initialize(context)
    }

    // Initialize location for SOS
    LaunchedEffect(Unit) {
        sosViewModel.initLocationClient(context)
    }

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
                // Not needed here, handled by userType selection
            }
        )
    }
    else if (isLoggedIn == true) {
        // Determine what to show based on user type
        when (userData?.userType) {
            "helper" -> {
                // Helper only
                HelperModeApp(
                    onSwitchToUserMode = null,
                    onLogout = {
                        auth.signOut()
                        isLoggedIn = false
                        userData = null
                        movementViewModel.stopMonitoring()
                    },
                    userData = userData
                )
            }
            "user" -> {
                // User only - check if showing movement screen or normal app
                if (showMovementScreen) {
                    MovementDetectionScreen(
                        onBack = { showMovementScreen = false },
                        onAbnormalMovementDetected = { type, confidence ->
                            println("🚨 Abnormal movement detected: $type ($confidence)")
                        },
                        // FIXED: Pass SOS trigger
                        onTriggerSOS = { reason ->
                            println("SOS Triggered by Movement: $reason")
                            sosViewModel.sendSosAlert(context)
                        }
                    )
                } else {
                    UserModeApp(
                        currentDestination = currentDestination,
                        onDestinationChange = { currentDestination = it },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                            userData = null
                            movementViewModel.stopMonitoring()
                        },
                        showSwitchToHelper = false,
                        movementViewModel = movementViewModel,
                        onMovementScreenClick = { showMovementScreen = true }
                    )
                }
            }
            "user_helper" -> {
                // User+Helper - can switch between modes
                if (appMode == AppMode.USER) {
                    if (showMovementScreen) {
                        MovementDetectionScreen(
                            onBack = { showMovementScreen = false },
                            onAbnormalMovementDetected = { type, confidence ->
                                println("🚨 Abnormal movement detected: $type ($confidence)")
                            },
                            // FIXED: Pass SOS trigger
                            onTriggerSOS = { reason ->
                                println("SOS Triggered by Movement: $reason")
                                sosViewModel.sendSosAlert(context)
                            }
                        )
                    } else {
                        UserModeApp(
                            currentDestination = currentDestination,
                            onDestinationChange = { currentDestination = it },
                            onLogout = {
                                auth.signOut()
                                isLoggedIn = false
                                userData = null
                                movementViewModel.stopMonitoring()
                            },
                            showSwitchToHelper = true,
                            onSwitchToHelperMode = { appMode = AppMode.HELPER },
                            movementViewModel = movementViewModel,
                            onMovementScreenClick = { showMovementScreen = true }
                        )
                    }
                } else {
                    HelperModeApp(
                        onSwitchToUserMode = {
                            appMode = AppMode.USER
                            movementViewModel.stopMonitoring()
                        },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                            userData = null
                            movementViewModel.stopMonitoring()
                        },
                        userData = userData
                    )
                }
            }
            else -> {
                // Default fallback
                if (showMovementScreen) {
                    MovementDetectionScreen(
                        onBack = { showMovementScreen = false },
                        onAbnormalMovementDetected = { type, confidence ->
                            println("🚨 Abnormal movement detected: $type ($confidence)")
                        },
                        // FIXED: Pass SOS trigger
                        onTriggerSOS = { reason ->
                            println("SOS Triggered by Movement: $reason")
                            sosViewModel.sendSosAlert(context)
                        }
                    )
                } else {
                    UserModeApp(
                        currentDestination = currentDestination,
                        onDestinationChange = { currentDestination = it },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                            userData = null
                            movementViewModel.stopMonitoring()
                        },
                        showSwitchToHelper = false,
                        movementViewModel = movementViewModel,
                        onMovementScreenClick = { showMovementScreen = true }
                    )
                }
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
    movementViewModel: MovementViewModel,
    onMovementScreenClick: () -> Unit,
    onSwitchToHelperMode: (() -> Unit)? = null
) {
    // Collect movement state - kept for badge indicator in HomeScreen
    val movementState by movementViewModel.movementState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Show only main tabs in bottom navigation (first 5)
                listOf(
                    AppDestinations.HOME,
                    AppDestinations.CONTACTS,
                    AppDestinations.MAP,
                    AppDestinations.AI,
                    AppDestinations.PROFILE
                ).forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { onDestinationChange(destination) },
                        icon = {
                            Icon(destination.icon, destination.label)
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showSwitchToHelper && onSwitchToHelperMode != null) {
                FloatingActionButton(
                    onClick = {
                        onSwitchToHelperMode.invoke()
                    },
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
                AppDestinations.HOME -> HomeScreen(
                    movementViewModel = movementViewModel,
                    onCardOneClick = { onDestinationChange(AppDestinations.HOME) },
                    onCardTwoClick = { onDestinationChange(AppDestinations.HOME) },
                    onCardFiveClick = { onDestinationChange(AppDestinations.HOME) },
                    onMovementScreenClick = onMovementScreenClick
                )
                AppDestinations.CONTACTS -> TrustedContactsScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.MAP -> GeneralMapScreen()
                AppDestinations.AI -> AiHelpScreen()
                AppDestinations.PROFILE -> ProfileScreen(
                    onBack = { onDestinationChange(AppDestinations.HOME) },
                    onLogout = onLogout,
                    onNavigateToContacts = { onDestinationChange(AppDestinations.CONTACTS) },
                    onNavigateToSOS = { onDestinationChange(AppDestinations.SOS_SETTINGS) },
                    onNavigateToNotifications = { onDestinationChange(AppDestinations.NOTIFICATIONS) },
                    onNavigateToPrivacy = { onDestinationChange(AppDestinations.PRIVACY) },
                    onNavigateToDataStorage = { onDestinationChange(AppDestinations.DATA_STORAGE) },
                    onNavigateToHelpCenter = { onDestinationChange(AppDestinations.HELP_CENTER) },
                    onNavigateToContactSupport = { onDestinationChange(AppDestinations.CONTACT_SUPPORT) },
                    onNavigateToAbout = { onDestinationChange(AppDestinations.ABOUT) }
                )
                // Add new destination screens
                AppDestinations.SOS_SETTINGS -> SosSettingsScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.NOTIFICATIONS -> NotificationsSettingsScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.PRIVACY -> PrivacySettingsScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.DATA_STORAGE -> DataStorageScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.HELP_CENTER -> HelpCenterScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.CONTACT_SUPPORT -> ContactSupportScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
                )
                AppDestinations.ABOUT -> AboutScreen(
                    onBack = { onDestinationChange(AppDestinations.PROFILE) }
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
                        IconButton(
                            onClick = onSwitchToUserMode
                        ) {
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
            when (currentScreen) {
                HelperScreen.DASHBOARD -> Column(modifier = Modifier.fillMaxSize()) {
                    HelperDashboard(
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
                }
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
                // FIXED: Support screen navigation connected here
                HelperScreen.SUPPORT -> HelperSupportScreen(
                    onBack = { currentScreen = HelperScreen.DASHBOARD }
                )
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