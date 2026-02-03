package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sheshield.models.VerificationStatus
import com.example.sheshield.screens.helper.LivePhotoCapture
import com.example.sheshield.screens.helper.NIDUploadScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File

@Composable
fun VerificationScreen(
    onVerificationComplete: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(VerificationStatus()) }
    var showLivePhoto by remember { mutableStateOf(false) }
    var lastCapturedFile by remember { mutableStateOf<File?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var showNIDUpload by remember { mutableStateOf(false) }

    val auth = Firebase.auth
    val currentUser = auth.currentUser

    val userId = remember {
        currentUser?.uid ?:
        currentUser?.email?.replace("@", "_at_")?.replace(".", "_dot_")
        ?: "user_${System.currentTimeMillis()}"
    }

    if (showNIDUpload) {
        NIDUploadScreen(
            userId = userId,
            onUploadComplete = {
                status = status.copy(nid = true)
                showNIDUpload = false
            },
            onBack = { showNIDUpload = false }
        )
        return
    }

    if (showLivePhoto) {
        LivePhotoCapture { success, file ->
            if (success) {
                status = status.copy(livePhoto = true)
                lastCapturedFile = file
                statusMessage = "✅ Face verified successfully"
            } else {
                statusMessage = "❌ Face not detected. Try again"
            }
            showLivePhoto = false
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PurpleGlow)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF12001F),
                        Color(0xFF1B0033),
                        Color(0xFF0F001A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.18f),
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Verification",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = PurpleGlow
                )
            }

            GlassButton(
                text = "Live Photo Verification",
                done = status.livePhoto
            ) {
                statusMessage = ""
                showLivePhoto = true
            }

            GlassButton(
                text = "Upload NID",
                done = status.nid
            ) {
                showNIDUpload = true
            }

            Button(
                onClick = onVerificationComplete,
                enabled = status.isFullyVerified(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleGlow
                )
            ) {
                Text("Continue", color = Color.White)
            }

            TextButton(onClick = onBackToLogin) {
                Text(
                    "Back to Login",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun GlassButton(
    text: String,
    done: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (done)
                    PurpleGlow.copy(alpha = 0.25f)
                else
                    Color.White.copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                PurpleGlow.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (done) "✔ $text" else text,
                color = Color.White
            )
        }
    }
}

private val PurpleGlow = Color(0xFFB388FF)
