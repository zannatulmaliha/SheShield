package com.example.sheshield.screens.app

import androidx.compose.runtime.Composable
import com.example.sheshield.screens.LoginSignupScreen
import com.example.sheshield.screens.SOSScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SheShieldApp() {
    val auth = FirebaseAuth.getInstance()
    val isLoggedIn = auth.currentUser != null

    if (isLoggedIn) {
        SOSScreen(
            onLogout = {
                auth.signOut()
            }
        )
    } else {
        LoginSignupScreen(
            onLoginSuccess = {
                // Handled inside LoginSignupScreen
            }
        )
    }
}
