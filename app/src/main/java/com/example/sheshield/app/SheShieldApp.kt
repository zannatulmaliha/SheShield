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

@Composable
fun SheShieldApp() {
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    // Listen to auth state changes
    LaunchedEffect(key1 = auth.currentUser) {
        isLoggedIn = auth.currentUser != null
    }

    if (isLoggedIn) {
        ProfileScreen(
            onBack = {},
            onLogout = {
                auth.signOut()
                isLoggedIn = false
            }
        )
    } else {
        LoginSignupScreen(
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
    }
}