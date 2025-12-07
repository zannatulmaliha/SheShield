// File: navigation/Screens.kt
package com.example.sheshield.navigation

// Think of this as your ROUTES file in React
sealed class Screen(
    val title: String,      // Screen name for display
    val route: String       // Unique identifier (like URL)
) {
    // Your existing screens
    object Profile : Screen("Profile", "profile")
    object TrustedContacts : Screen("Contacts", "contacts")

    // Future screens (add here later)
    // object Dashboard : Screen("Dashboard", "dashboard")
    // object SafetyMap : Screen("Safety Map", "map")
}