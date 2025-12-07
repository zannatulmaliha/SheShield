// File: navigation/NavController.kt
package com.example.sheshield.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// Think of this as your useNavigate/useState in React
class NavController {
    // Tracks current screen (starts with Profile)
    var currentScreen by mutableStateOf<Screen>(Screen.Profile)
        private set  // Can't change from outside

    // Function to navigate (like navigate('/contacts') in React)
    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
}

// Helper to create and remember nav controller
@Composable
fun rememberNavController(): NavController {
    return rememberSaveable {
        NavController()
    }
}