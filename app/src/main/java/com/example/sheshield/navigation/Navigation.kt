//package com.example.sheshield.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import com.example.sheshield.screens.*
//import com.example.sheshield.components.BoxWithBottomNav
//// Define all your screens
//sealed class Screen(val title: String, val route: String) {
//    object Dashboard : Screen("Dashboard", "dashboard")
//    object TrustedContacts : Screen("Contacts", "contacts")
//    object SafetyMap : Screen("Safety Map", "safety-map")
//    object AIAssistant : Screen("AI Help", "ai-assistant")
//    object Profile : Screen("Profile", "profile")
//    object SOSActive : Screen("SOS Active", "sos-active")
//    object IncidentReport : Screen("Incident Report", "incident-report")
//    // Add other screens as needed
//}
//
//@Composable
//fun AppNavigation() {
//    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Dashboard) }
//    var sosActive by rememberSaveable { mutableStateOf(false) }
//
//    // Screens that should hide bottom navigation
//    val screensWithoutBottomNav = listOf(
//        Screen.SOSActive.route,
//        Screen.IncidentReport.route,
//        // Add other screens that should hide bottom nav
//    )
//
//    // Check if current screen should show bottom nav
//    val showBottomNav = currentScreen.route !in screensWithoutBottomNav
//
//    // Main content with bottom navigation
//    BoxWithBottomNav(
//        currentScreen = currentScreen,
//        showBottomNav = showBottomNav,
//        sosActive = sosActive,
//        onScreenChange = { screen -> currentScreen = screen },
//        onSOSActivate = {
//            sosActive = true
//            currentScreen = Screen.SOSActive
//        }
//    )
//}

package com.example.sheshield.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun AppNavigation() {
    val context = LocalContext.current

    // Just show a toast to prove this function is called
    androidx.compose.runtime.LaunchedEffect(Unit) {
        Toast.makeText(context, "AppNavigation called", Toast.LENGTH_LONG).show()
    }

    // Show simple text
    androidx.compose.foundation.layout.Box(
//        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text("✅ Navigation Working!")
    }
}