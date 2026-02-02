package com.example.sheshield.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sheshield.models.VerificationStatus
import com.example.sheshield.screens.helper.LivePhotoCapture

import com.example.sheshield.screens.helper.NIDUploadScreen
import java.io.File

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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

    var showPhoneVerification by remember { mutableStateOf(false) }

    //var userId by remember { mutableStateOf("") } // You need to get actual user ID

    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Generate user ID for filename
    val userId = remember {
        currentUser?.uid ?:
        currentUser?.email?.let { email ->
            email.replace("@", "_at_")
                .replace(".", "_dot_")
                .toLowerCase()
        } ?: "user_${System.currentTimeMillis()}"
    }


    var showNIDUpload by remember { mutableStateOf(false) } // Add this





    if (showNIDUpload) {
        NIDUploadScreen(
            userId = userId,
            onUploadComplete = { paths ->
                // Check what's in the single folder
                val allFiles = File(context.filesDir, "NID_ALL")
                    .listFiles()?.map { it.name } ?: emptyList()
                println("📁 All NID files in single folder: $allFiles")

                showNIDUpload = false
                status = status.copy(nid = true)
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
                statusMessage = "✅ Human face detected!"
            }
            else {
                // Show message in parent UI
                 statusMessage = "❌ No human face detected. Try again."
            }
            showLivePhoto = false // close camera after capture
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Opening camera...")
        }

        return
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center  // <-- centers the Column
    ) {
        Column(
            modifier = Modifier.wrapContentHeight(),
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Complete Verification", style = MaterialTheme.typography.headlineSmall)

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage, color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
            }




            VerificationButton(
                text = "Live Photo Verification",
                done = status.livePhoto
            ) {
                statusMessage = "" // reset message
                showLivePhoto = true
//            status = status.copy(livePhoto = true)
            }

            VerificationButton(
                text = "Upload NID",
                done = status.nid
            ) {
                showNIDUpload = true
//            status = status.copy(nid = true) // placeholder
            }



//            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerificationComplete,
                enabled = status.isFullyVerified()
            ) {
                Text("Continue")
            }
//            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Add a back button to go to login
            OutlinedButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Login")
            }
        }
    }
//    if (showPhoneVerification) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth(0.9f)
//                    .wrapContentHeight(),
//                shape = MaterialTheme.shapes.large,
//                elevation = CardDefaults.cardElevation(8.dp)
//            ) {
//                PhoneVerificationScreen(
//                    onVerified = {
//                        status = status.copy(phone = true)
//                        showPhoneVerification = false
//                    },
//                    onBack = { showPhoneVerification = false }
//                )
//            }
//        }
//    }
//    if (showPhoneVerification) {
//
//    }

}

@Composable
private fun VerificationButton(
    text: String,
    done: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (done)
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        else
            ButtonDefaults.buttonColors()
    ) {
        Text(if (done) "✔ $text" else text)
    }
}
