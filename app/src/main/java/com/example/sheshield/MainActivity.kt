package com.example.sheshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
import com.example.sheshield.models.UserData
import com.example.sheshield.screens.*
import com.example.sheshield.screens.helper.HelperAlertsContent
import com.example.sheshield.screens.helper.HelperScreen
import com.example.sheshield.screens.helper.HelperAlertsScreen
import com.example.sheshield.screens.helper.HelperDashboard
import com.example.sheshield.screens.helper.HelperProfileScreen
import com.example.sheshield.ui.theme.SheShieldTheme
import com.example.sheshield.viewmodel.MovementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

import com.example.sheshield.screens.VerificationScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.tasks.await


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
    val context = LocalContext.current
    val movementViewModel: MovementViewModel = viewModel()

    // State variables
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var appMode by rememberSaveable { mutableStateOf(AppMode.USER) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var showMovementScreen by rememberSaveable { mutableStateOf(false) }

    var needsVerification by remember { mutableStateOf(true) }

    val currentUser = Firebase.auth.currentUser

//    LaunchedEffect(currentUser?.uid) {
//        currentUser?.uid?.let { uid ->
//            val docRef = Firebase.firestore.collection("users").document(uid)
//            docRef.get()
//                .addOnSuccessListener { doc ->
//                    val fetchedUser = doc.toObject(UserData::class.java)
//                    // Force copy UID
//                    val isVerifiedFromFirestore = doc.getBoolean("isHelperVerified") ?: false
//
//                    userData = fetchedUser?.copy(
//                        userId = uid,
//                        isHelperVerified = isVerifiedFromFirestore
//                    )
//
//                    println("🔥 userData after fix = $userData")
//
//
//
////                    userData = fetchedUser?.copy(userId = uid)
////
////
////                    // Debug: check real value from Firestore
////                    val rawIsVerified = doc.getBoolean("isHelperVerified")
////                    println("🔥 Firestore field isHelperVerified = $rawIsVerified")
////                    println("🔥 userData after copy = $userData")
//
//                    isLoading = false
//                }
//                .addOnFailureListener {
//                    userData = null
//                    isLoading = false
//                }
//        } ?: run { isLoading = false }
////            val doc = Firebase.firestore.collection("users").document(uid).get()
////                .addOnSuccessListener { doc ->
////            userData = doc.toObject(UserData::class.java)
////
////                    println("🔥 userData fetched: $userData")
////                    println("🔥 isHelperVerified = ${userData?.isHelperVerified}")
////                    }
////        }
////    }
//    LaunchedEffect(currentUser?.uid) {
//        isLoading = true
//        currentUser?.uid?.let { uid ->
//            try {
//                val doc = Firebase.firestore.collection("users").document(uid).get().await()
//                val fetchedUser = doc.toObject(UserData::class.java)
//                val isVerified = doc.getBoolean("isHelperVerified") ?: false
//                userData = fetchedUser?.copy(userId = uid, isHelperVerified = isVerified)
//
//
//                    println("🔥 userData after fix = $userData")
//
//                isLoggedIn = true
//            } catch (e: Exception) {
//                userData = null
//                isLoggedIn = false
//            }
//        }
//        isLoading = false
//    }



    val requiresVerification =
        (userData?.userType == "helper" || userData?.userType == "user_helper")


    // Initialize movement view model
    LaunchedEffect(Unit) {
        movementViewModel.initialize(context)
    }



    LaunchedEffect(currentUser?.uid) {
        isLoading = true
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            try {
                val doc = Firebase.firestore.collection("users").document(currentUser.uid).get().await()
                val fetchedUser = doc.toObject(UserData::class.java)
                val isVerified = doc.getBoolean("isHelperVerified") ?: false
                userData = fetchedUser?.copy(userId = currentUser.uid, isHelperVerified = isVerified)



                println("🔥 userData after fix = $userData")



                 //✅ Set login state here
                isLoggedIn = true


            } catch (e: Exception) {
                userData = null
                isLoggedIn = false
            }
        } else {
            isLoggedIn = false
            userData = null
        }

        isLoading = false
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


        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        }
        else if ((userData?.userType == "helper"|| userData?.userType == "user_helper") && userData?.isHelperVerified == false) {
            VerificationScreen(
                onVerificationComplete = {
                    // Update Firestore
                    Firebase.firestore.collection("users")
                        .document(currentUser!!.uid)
                        .update("isHelperVerified", true)

                    userData = userData?.copy(isHelperVerified = true)

                },
                onBackToLogin = {
                    // Force logout / go back to login
                    Firebase.auth.signOut()
                    isLoggedIn = false
                    userData = null
                }
            )
        }
        // ✅ STEP 2: ONLY AFTER VERIFICATION → ENTER APP
        else {
            when (userData?.userType) {
                "helper" -> {
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
                    if (showMovementScreen) {
                        MovementDetectionScreen(
                            onBack = { showMovementScreen = false },
                            onAbnormalMovementDetected = { type, confidence ->
                                println("🚨 Abnormal movement detected: $type ($confidence)")
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
                    if (appMode == AppMode.USER) {
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
                    } else {
                        HelperModeApp(
                            onSwitchToUserMode = {
                                appMode = AppMode.USER
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
            }
        }














        // Determine what to show based on user type
//        when (userData?.userType) {
//            "helper" -> {
//                // Helper only
//                HelperModeApp(
//                    onSwitchToUserMode = null,
//                    onLogout = {
//                        auth.signOut()
//                        isLoggedIn = false
//                        userData = null
//                        movementViewModel.stopMonitoring()
//                    },
//                    userData = userData
//                )
//            }
//            "user" -> {
//                // User only - check if showing movement screen or normal app
//                if (showMovementScreen) {
//                    MovementDetectionScreen(
//                        onBack = { showMovementScreen = false },
//                        onAbnormalMovementDetected = { type, confidence ->
//                            println("🚨 Abnormal movement detected: $type ($confidence)")
//                        }
//                    )
//                } else {
//                    UserModeApp(
//                        currentDestination = currentDestination,
//                        onDestinationChange = { currentDestination = it },
//                        onLogout = {
//                            auth.signOut()
//                            isLoggedIn = false
//                            userData = null
//                            movementViewModel.stopMonitoring()
//                        },
//                        showSwitchToHelper = false,
//                        movementViewModel = movementViewModel,
//                        onMovementScreenClick = { showMovementScreen = true }
//                    )
//                }
//            }
//            "user_helper" -> {
//                // User+Helper - can switch between modes
//                if (appMode == AppMode.USER) {
//                    if (showMovementScreen) {
//                        MovementDetectionScreen(
//                            onBack = { showMovementScreen = false },
//                            onAbnormalMovementDetected = { type, confidence ->
//                                println("🚨 Abnormal movement detected: $type ($confidence)")
//                            }
//                        )
//                    } else {
//                        UserModeApp(
//                            currentDestination = currentDestination,
//                            onDestinationChange = { currentDestination = it },
//                            onLogout = {
//                                auth.signOut()
//                                isLoggedIn = false
//                                userData = null
//                                movementViewModel.stopMonitoring()
//                            },
//                            showSwitchToHelper = true,
//                            onSwitchToHelperMode = { appMode = AppMode.HELPER },
//                            movementViewModel = movementViewModel,
//                            onMovementScreenClick = { showMovementScreen = true }
//                        )
//                    }
//                } else {
//                    HelperModeApp(
//                        onSwitchToUserMode = {
//                            appMode = AppMode.USER
//                            movementViewModel.stopMonitoring()
//                        },
//                        onLogout = {
//                            auth.signOut()
//                            isLoggedIn = false
//                            userData = null
//                            movementViewModel.stopMonitoring()
//                        },
//                        userData = userData
//                    )
//                }
//            }
//            else -> {
//                // Default fallback
//                if (showMovementScreen) {
//                    MovementDetectionScreen(
//                        onBack = { showMovementScreen = false },
//                        onAbnormalMovementDetected = { type, confidence ->
//                            println("🚨 Abnormal movement detected: $type ($confidence)")
//                        }
//                    )
//                } else {
//                    UserModeApp(
//                        currentDestination = currentDestination,
//                        onDestinationChange = { currentDestination = it },
//                        onLogout = {
//                            auth.signOut()
//                            isLoggedIn = false
//                            userData = null
//                            movementViewModel.stopMonitoring()
//                        },
//                        showSwitchToHelper = false,
//                        movementViewModel = movementViewModel,
//                        onMovementScreenClick = { showMovementScreen = true }
//                    )
//                }
//            }
//        }
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
                AppDestinations.entries.forEach { destination ->
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
            //if (showSwitchToHelper && onSwitchToHelperMode != null) {
                FloatingActionButton(
                    onClick = {
                        onSwitchToHelperMode?.invoke();
                        //appMode = AppMode.HELPER; // wny error here? unresolved reference appMode
                    }, //onSwitchToHelperMode,
                    containerColor = Color(0xFF6200EE)
                ) {
                    Icon(
                        Icons.Default.SupervisorAccount,
                        "Switch to Helper Mode",
                        tint = Color.White
                    )
                }
            //}
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
                    onBack = { onDestinationChange(AppDestinations.HOME) }
                )
                AppDestinations.MAP -> GeneralMapScreen()
                AppDestinations.AI -> Text("AI Help Screen")
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
                HelperScreen.ALERTS -> HelperAlertsContent(
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