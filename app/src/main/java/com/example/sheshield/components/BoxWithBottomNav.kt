//package com.example.sheshield.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.sheshield.navigation.Screen
//import com.example.sheshield.screens.*
//import com.google.firebase.auth.FirebaseAuth
//
//@Composable
//fun BoxWithBottomNav(
//    currentScreen: Screen,
//    showBottomNav: Boolean,
//    sosActive: Boolean,
//    onScreenChange: (Screen) -> Unit,
//    onSOSActivate: () -> Unit
//) {
//    // Main content area
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF9FAFB)) // gray-50 background
//    ) {
//        // Render the current screen
//        when (currentScreen) {
//            is Screen.Dashboard -> {
//                // FIX 1: Use a placeholder until DashboardScreen is created
//                Text("Dashboard Screen Placeholder", modifier = Modifier.align(Alignment.Center))
//                /* Uncomment this when DashboardScreen is ready
//                DashboardScreen(
//                    onNavigate = { route ->
//                        val target = when (route) {
//                            "contacts" -> Screen.TrustedContacts
//                            "safety-map" -> Screen.SafetyMap
//                            "ai-assistant" -> Screen.AIAssistant
//                            "profile" -> Screen.Profile
//                            "sos-active" -> Screen.SOSActive
//                            else -> Screen.Dashboard
//                        }
//                        onScreenChange(target)
//                        if (route == "sos-active") onSOSActivate()
//                    },
//                    onSOSActivate = onSOSActivate
//                )
//                */
//            }
//            is Screen.TrustedContacts -> {
//                // This one exists, so we keep it
//                TrustedContactsScreen(
//                    onBack = { onScreenChange(Screen.Dashboard) }
//                )
//            }
//            is Screen.SafetyMap -> {
//                // FIX 2: Placeholder
//                Text("Safety Map Placeholder", modifier = Modifier.align(Alignment.Center))
//            }
//            is Screen.AIAssistant -> {
//                // FIX 3: Placeholder
//                Text("AI Assistant Placeholder", modifier = Modifier.align(Alignment.Center))
//            }
//            is Screen.Profile -> {
//                // This one exists, keep it
//                ProfileScreen(
//                    onBack = { onScreenChange(Screen.Dashboard) },
//                    onLogout = {
//                        FirebaseAuth.getInstance().signOut()
//                    },
////                    onNavigateToContacts = { onScreenChange(Screen.TrustedContacts) }
//                )
//            }
//            is Screen.SOSActive -> {
//                // FIX 4: Placeholder
//                Text("SOS Active Screen Placeholder", modifier = Modifier.align(Alignment.Center))
//            }
//            is Screen.IncidentReport -> {
//                // FIX 5: Placeholder
//                Text("Incident Report Placeholder", modifier = Modifier.align(Alignment.Center))
//            }
//        }
//
//        // Bottom Navigation
//        if (showBottomNav) {
//            BottomNavigationBar(
//                currentScreen = currentScreen,
//                onNavigate = onScreenChange,
//                // FIX 6: Pass onSOSClick if your BottomNavBar requires it
////                onSOSClick = onSOSActivate,
//                modifier = Modifier.align(Alignment.BottomCenter)
//            )
//        }
//
//        // SOS Alert Indicator
//        if (sosActive && currentScreen !is Screen.SOSActive) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(40.dp)
//                    .background(Color.Red)
//                    .align(Alignment.TopCenter)
//            ) {
//                Text(
//                    text = "🚨 SOS ACTIVE - Emergency services notified",
//                    color = Color.White,
//                    modifier = Modifier.align(Alignment.Center),
//                    fontSize = 14.sp
//                )
//            }
//        }
//    }
//}
