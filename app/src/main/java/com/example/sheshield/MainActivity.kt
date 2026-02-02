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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.SOS.SosViewModel
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.*
import com.example.sheshield.navigation.HelperScreen
import com.example.sheshield.screens.helper.HelperAlertsScreen
import com.example.sheshield.screens.helper.HelperDashboard
import com.example.sheshield.screens.helper.HelperProfileScreen
import com.example.sheshield.screens.helper.HelperSupportScreen
import com.example.sheshield.ui.theme.SheShieldTheme
import com.example.sheshield.viewmodel.MovementViewModel
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

    val movementViewModel: MovementViewModel = viewModel()
    val sosViewModel: SosViewModel = viewModel()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var appMode by rememberSaveable { mutableStateOf(AppMode.USER) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var showMovementScreen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { movementViewModel.initialize(context) }
    LaunchedEffect(Unit) { sosViewModel.initLocationClient(context) }

    LaunchedEffect(key1 = auth.currentUser) {
        isLoading = true
        delay(500)
        val currentUser = auth.currentUser
        isLoggedIn = currentUser != null

        if (isLoggedIn == true && currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.toObject(UserData::class.java)?.copy(userId = currentUser.uid)
                        appMode = if (userData?.userType == "helper") AppMode.HELPER else AppMode.USER
                    }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (isLoggedIn == false) {
        LoginSignupScreen(
            onLoginSuccess = { isLoggedIn = true; currentDestination = AppDestinations.HOME },
            onSwitchToHelperMode = {}
        )
    } else {
        if ((userData?.userType == "helper" || userData?.userType == "user_helper") && userData?.isHelperVerified == false) {
            VerificationScreen(
                onVerificationComplete = {
                    firestore.collection("users").document(auth.currentUser!!.uid).update("isHelperVerified", true)
                    userData = userData?.copy(isHelperVerified = true)
                },
                onBackToLogin = { auth.signOut(); isLoggedIn = false }
            )
        } else {
            when (userData?.userType) {
                "helper" -> HelperModeApp(
                    onSwitchToUserMode = null,
                    onLogout = { auth.signOut(); isLoggedIn = false; movementViewModel.stopMonitoring() },
                    userData = userData
                )
                else -> {
                    if (appMode == AppMode.USER) {
                        if (showMovementScreen) {
                            MovementDetectionScreen(
                                onBack = { showMovementScreen = false },
                                onAbnormalMovementDetected = { _, _ -> },
                                onTriggerSOS = { sosViewModel.sendSosAlert(context) }
                            )
                        } else {
                            UserModeApp(
                                currentDestination = currentDestination,
                                onDestinationChange = { currentDestination = it },
                                onLogout = { auth.signOut(); isLoggedIn = false; movementViewModel.stopMonitoring() },
                                showSwitchToHelper = userData?.userType == "user_helper",
                                movementViewModel = movementViewModel,
                                sosViewModel = sosViewModel,
                                onMovementScreenClick = { showMovementScreen = true },
                                onSwitchToHelperMode = { appMode = AppMode.HELPER }
                            )
                        }
                    } else {
                        HelperModeApp(
                            onSwitchToUserMode = { appMode = AppMode.USER; movementViewModel.stopMonitoring() },
                            onLogout = { auth.signOut(); isLoggedIn = false; movementViewModel.stopMonitoring() },
                            userData = userData
                        )
                    }
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
    sosViewModel: SosViewModel,
    onMovementScreenClick: () -> Unit,
    onSwitchToHelperMode: (() -> Unit)? = null
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(AppDestinations.HOME, AppDestinations.CONTACTS, AppDestinations.MAP, AppDestinations.AI, AppDestinations.PROFILE).forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination == dest,
                        onClick = { onDestinationChange(dest) },
                        icon = { Icon(dest.icon, dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showSwitchToHelper && onSwitchToHelperMode != null) {
                FloatingActionButton(onClick = onSwitchToHelperMode, containerColor = Color(0xFF6200EE)) {
                    Icon(Icons.Default.SupervisorAccount, "Switch to Helper", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    movementViewModel = movementViewModel,
                    sosViewModel = sosViewModel, // ✅ FIXED: Passing the actual ViewModel object
                    onCardOneClick = { },
                    onCardTwoClick = { },
                    onCardFiveClick = { },
                    onMovementScreenClick = onMovementScreenClick
                )
                AppDestinations.CONTACTS -> TrustedContactsScreen { onDestinationChange(AppDestinations.PROFILE) }
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
                AppDestinations.SOS_SETTINGS -> SosSettingsScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.NOTIFICATIONS -> NotificationsSettingsScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.PRIVACY -> PrivacySettingsScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.DATA_STORAGE -> DataStorageScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.HELP_CENTER -> HelpCenterScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.CONTACT_SUPPORT -> ContactSupportScreen { onDestinationChange(AppDestinations.PROFILE) }
                AppDestinations.ABOUT -> AboutScreen { onDestinationChange(AppDestinations.PROFILE) }
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
    var currentScreen by rememberSaveable { mutableStateOf(HelperScreen.DASHBOARD) }
    // ✅ FIX: Extract context here to avoid "@Composable invocations" error inside the lambda
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Helper Mode", color = Color.White, fontWeight = FontWeight.Bold)
                        if (userData?.gender == "male") {
                            Spacer(Modifier.width(8.dp))
                            Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                                Text("MALE HELPER", color = Color(0xFF1976D2), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                },
                actions = {
                    onSwitchToUserMode?.let {
                        IconButton(onClick = it) { Icon(Icons.Filled.SwitchAccount, null, tint = Color.White) }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1976D2))
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
                                imageVector = when(screen) {
                                    HelperScreen.DASHBOARD -> Icons.Filled.Dashboard
                                    HelperScreen.ALERTS -> Icons.Filled.Notifications
                                    HelperScreen.PROFILE -> Icons.Filled.Person
                                    HelperScreen.SUPPORT -> Icons.Filled.Info
                                    HelperScreen.HISTORY -> Icons.Filled.List
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.name.lowercase().capitalize()) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                HelperScreen.DASHBOARD -> HelperDashboard(
                    onNavigate = { currentScreen = it },
                    onSwitchToUserMode = onSwitchToUserMode,
                    onAcceptAlert = {
                        // ✅ FIX: Use the 'context' variable from above
                        Toast.makeText(context, "Alert Accepted", Toast.LENGTH_SHORT).show()
                        currentScreen = HelperScreen.ALERTS
                    },
                    userData = userData
                )
                HelperScreen.ALERTS -> HelperAlertsScreen(onBack = { currentScreen = HelperScreen.DASHBOARD }, onNavigateToMap = {})
                HelperScreen.PROFILE -> HelperProfileScreen(onBack = { currentScreen = HelperScreen.DASHBOARD }, onLogout = onLogout, userData = userData)
                HelperScreen.SUPPORT -> HelperSupportScreen(onBack = { currentScreen = HelperScreen.DASHBOARD })
                HelperScreen.HISTORY -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("History Placeholder") }
            }
        }
    }
}