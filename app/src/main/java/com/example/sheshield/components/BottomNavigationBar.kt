
package com.example.sheshield.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.navigation.Screen

// Define colors
val Purple600 = Color(0xFF7C3AED)
val Gray400 = Color(0xFF9CA3AF)
val Gray800 = Color(0xFF1F2937)

@Composable
fun BottomNavigationBar(
currentScreen: Screen,       // Which screen is active
onNavigate: (Screen) -> Unit // Callback when icon clicked
)
{
    // Create a container for the navigation bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp), // Height of the navigation bar
        color = Color.White, // White background
        shadowElevation = 4.dp, // Small shadow
        shape = RoundedCornerShape( // Rounded corners at top
            topStart = 20.dp,
            topEnd = 20.dp
        )
    ) {
        // Arrange icons horizontally
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround, // Equal space between icons
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. HOME ICON
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen is Screen.Profile, // Home shows Profile
                onClick = { onNavigate(Screen.Profile) } // Navigate to Profile
                //isSelected = true // Home is selected by default
            )

            // 2. CONTACTS ICON
            BottomNavItem(
                icon = Icons.Default.People,
                label = "Contacts",
//                isSelected = false
                isSelected = currentScreen is Screen.TrustedContacts,
                onClick = { onNavigate(Screen.TrustedContacts) }

            )

            // 3. MAP ICON
            BottomNavItem(
                icon = Icons.Default.Map,
                label = "Map",
                isSelected = currentScreen is Screen.Profile, // Home shows Profile
                onClick = { onNavigate(Screen.Profile) } // Navigate to Profile

            )

            // 4. CHAT/AI ICON
            BottomNavItem(
                icon = Icons.Default.Chat,
                label = "AI Help",
                isSelected = currentScreen is Screen.Profile, // Home shows Profile
                onClick = { onNavigate(Screen.Profile) } // Navigate to Profile

            )

            // 5. PROFILE ICON
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen is Screen.Profile, // Home shows Profile
                onClick = { onNavigate(Screen.Profile) } // Navigate to Profile
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,  // The icon image
    label: String,      // Text below icon
    isSelected: Boolean, // Is this item currently selected?
    onClick: () -> Unit
) {
    // Change color based on selection
    val iconColor = if (isSelected) Purple600 else Gray400
    val textColor = if (isSelected) Purple600 else Gray800
    val textWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    // Each navigation item
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
//            .clickable(onClick = {
//                // We'll add functionality later
//                println("Clicked: $label")
//            })
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .width(60.dp), // Fixed width for each item
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = label, // For accessibility
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        // Small spacing between icon and text
        Spacer(modifier = Modifier.height(4.dp))

        // Text label
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = textWeight
        )
    }
}