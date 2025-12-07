

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
        androidx.compose.material3.Text("Navigation Working!")
    }
}