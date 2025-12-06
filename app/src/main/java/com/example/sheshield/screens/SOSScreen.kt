package com.example.sheshield.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun SOSScreen(
    onLogout: () -> Unit
) {
    var countdown by remember { mutableStateOf(0) }
    var isAlertSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAlertSent) {
            Text(
                text = "🚨 ALERT SENT!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text("Help is on the way!")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { isAlertSent = false }) {
                Text("OK")
            }
        } else if (countdown > 0) {
            Text("Alert will send in...")
            Text(
                text = "$countdown",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.error
            )

            LaunchedEffect(countdown) {
                delay(1000)
                if (countdown > 0) {
                    countdown--
                }
                if (countdown == 0) {
                    sendAlert(auth, db) { success, message ->
                        isLoading = false
                        isAlertSent = success
                        statusMessage = message
                    }
                }
            }
        } else {
            Text(
                text = "EMERGENCY SOS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    countdown = 3
                    isLoading = true
                },
                modifier = Modifier.size(200.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "SOS\nHELP",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout
            ) {
                Text("Logout")
            }
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusMessage,
                color = if (statusMessage.startsWith("✅"))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun sendAlert(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    callback: (Boolean, String) -> Unit
) {
    val userId = auth.currentUser?.uid ?: run {
        callback(false, "Not logged in")
        return
    }

    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val userName = document.getString("name") ?: "User"

            val alertData = hashMapOf(
                "alertId" to UUID.randomUUID().toString(),
                "userId" to userId,
                "userName" to userName,
                "timestamp" to System.currentTimeMillis(),
                "status" to "active"
            )

            db.collection("active_alerts").document()
                .set(alertData)
                .addOnSuccessListener {
                    callback(true, "✅ Alert sent successfully!")
                }
                .addOnFailureListener { e ->
                    callback(false, "❌ Failed to send alert")
                }
        }
        .addOnFailureListener {
            callback(false, "❌ Failed to get user data")
        }
}
