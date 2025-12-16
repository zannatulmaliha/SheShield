package com.example.sheshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.sheshield.screens.LoginSignupScreen
import com.example.sheshield.screens.ProfileScreen
import com.example.sheshield.screens.TrustedContactsScreen
import com.example.sheshield.ui.theme.SheShieldTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SheShieldApp() {
    // Initialize Firebase Auth - Use this approach instead
    val auth = FirebaseAuth.getInstance()

    // State variables
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) } // null = loading, true/false = state
    var isLoading by remember { mutableStateOf(true) }

    // Listen to Firebase auth state changes
    LaunchedEffect(key1 = auth.currentUser) {
        // Check Firebase for existing login session
        isLoading = true

        // Add a small delay to ensure Firebase is initialized
        delay(500)

        // Firebase automatically persists login sessions
        // auth.currentUser will be non-null if user was previously logged in
        isLoggedIn = auth.currentUser != null

        // Reset to home screen when logging out
        if (isLoggedIn == false) {
            currentDestination = AppDestinations.HOME
        }

        isLoading = false
    }

    // Show loading indicator while checking auth state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    // Check login status and show appropriate screen
    else if (isLoggedIn == false) {
        // Show login screen when not logged in - THIS APPEARS FIRST
        LoginSignupScreen(
            onLoginSuccess = {
                isLoggedIn = true
                currentDestination = AppDestinations.HOME // Start at home after login
            }
        )
    } else if (isLoggedIn == true) {
        // Show main app with navigation when logged in
        Scaffold(
            bottomBar = {
                // Bottom navigation bar
                NavigationBar {
                    AppDestinations.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen()
                    AppDestinations.CONTACTS -> TrustedContactsScreen(
                        onBack = { currentDestination = AppDestinations.HOME }
                    )
                    AppDestinations.MAP -> Text("Map Screen - Coming Soon")
                    AppDestinations.AI -> Text("AI Help Screen - Coming Soon")
                    AppDestinations.PROFILE -> ProfileScreen(
                        onBack = { currentDestination = AppDestinations.HOME },
                        onLogout = {
                            // Firebase sign out - this will clear the persisted session
                            auth.signOut()
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SheShieldTheme {
        SheShieldApp()
    }
}