package com.example.sheshield.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sheshield.screens.LoginSignupScreen
import com.example.sheshield.screens.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.sheshield.screens.TrustedContactsScreen
import com.example.sheshield.components.BottomNavigationBar
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*


@Composable
fun Email() {
//    AppNavigation()
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    // 2. Track which screen to show (false = Profile, true = Contacts)
    //var showContactsScreen by remember { mutableStateOf(false) }

    // Navigation state
    var currentScreen by remember { mutableStateOf<com.example.sheshield.navigation.Screen>(
        com.example.sheshield.navigation.Screen.Profile
    ) }

    // Listen to auth state changes
    LaunchedEffect(key1 = auth.currentUser) {
        isLoggedIn = auth.currentUser != null
    }

    if (isLoggedIn) {
//        AppNavigation()
//        ProfileScreen(
//            onBack = {},
//            onLogout = {
//                auth.signOut()
//                isLoggedIn = false
//            }
//        )
        // CHANGED: Directly showing TrustedContactsScreen for testing
//        TrustedContactsScreen(
//            onBack = {
//                // Optional: If you want to test logging out from here temporarily
//                auth.signOut()
//                isLoggedIn = false
//            }
//        )

//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Your TrustedContactsScreen at the top
//            TrustedContactsScreen(
//                onBack = {
//                    auth.signOut()
//                    isLoggedIn = false
//                }
//            )
//
//            // Bottom Navigation Bar at the bottom
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//            ) {
//                BottomNavigationBar(
//                    currentScreen = currentScreen,
//                onNavigate = { screen ->
//                    currentScreen = screen
//                }
//
//                )
//
//
//
//            }
//        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Show current screen (fills entire box)
            when (currentScreen) {
                is com.example.sheshield.navigation.Screen.Profile -> {
                    ProfileScreen(
                        onBack = { },
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                        }
                    )
                }
                is com.example.sheshield.navigation.Screen.TrustedContacts -> {
                    TrustedContactsScreen(
                        onBack = { }
                    )
                }
            }



        }
    } else {
        LoginSignupScreen(
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
    }
}