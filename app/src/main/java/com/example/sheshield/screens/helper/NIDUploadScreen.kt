package com.example.sheshield.screens.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NIDUploadScreen(
    userId: String,
    onUploadComplete: (Map<String, String>) -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var frontImagePath by remember { mutableStateOf<String?>(null) }
    var backImagePath by remember { mutableStateOf<String?>(null) }

    // Gallery launchers
    val galleryLauncherFront = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedPath = saveNIDToSingleFolder(context, userId, it, "front")
            frontImagePath = savedPath
        }
    }

    val galleryLauncherBack = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedPath = saveNIDToSingleFolder(context, userId, it, "back")
            backImagePath = savedPath
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Text(
            text = "Upload NID Card",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // FRONT SIDE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FRONT SIDE",
                    style = MaterialTheme.typography.titleMedium
                )

                if (frontImagePath != null) {
                    val bitmap = loadBitmapFromPath(frontImagePath!!)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "NID Front",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                        )
                    }
                    Text("✅ Uploaded", color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = { galleryLauncherFront.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Front Image")
                    }
                }
            }
        }

        // BACK SIDE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BACK SIDE",
                    style = MaterialTheme.typography.titleMedium
                )

                if (backImagePath != null) {
                    val bitmap = loadBitmapFromPath(backImagePath!!)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "NID Back",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                        )
                    }
                    Text("✅ Uploaded", color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = { galleryLauncherBack.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Back Image")
                    }
                }
            }
        }

        // Submit button
        Button(
            onClick = {
                if (frontImagePath != null && backImagePath != null) {
                    val paths = mapOf(
                        "front" to frontImagePath!!,
                        "back" to backImagePath!!
                    )
                    onUploadComplete(paths)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = frontImagePath != null && backImagePath != null
        ) {
            Text("Submit NID")
        }

        // Debug info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text("📁 All NID files are saved in:", style = MaterialTheme.typography.labelMedium)
//                Text("${context.filesDir.absolutePath}/NID_ALL/",
//                    style = MaterialTheme.typography.bodySmall)
//                Text("Format: ${userId}_front_timestamp.jpg",
//                    style = MaterialTheme.typography.bodySmall)
//            }
        }
    }
}



private fun saveNIDToSingleFolder(
    context: Context,
    userId: String,  // Firebase UID like "Ue7fQoFU9cSN9WrM9fYR3ghVtHg2"
    uri: Uri,
    side: String
): String? {
    return try {
        // 1. ✅ CREATE SEPARATE FOLDER FOR THIS USER
        val userFolder = File(context.filesDir, "NID/$userId")
        userFolder.mkdirs()  // Creates: /NID/Ue7fQoFU9cSN9WrM9fYR3ghVtHg2/

        println("📁 Created folder for user: $userId")
        println("📁 Path: ${userFolder.absolutePath}")

        // 2. Generate filename with timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "nid_${side}_$timestamp.jpg"
        val file = File(userFolder, fileName)

        // 3. Save image
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        }

//        println("✅ Saved to user's folder: ${file.name}")
//        println("📁 User folder now has: ${userFolder.listFiles()?.size ?: 0} files")

        file.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
        println("❌ Error saving: ${e.message}")
        null
    }
}





// ✅ LOAD BITMAP
private fun loadBitmapFromPath(path: String): Bitmap? {
    return try {
        BitmapFactory.decodeFile(path)
    } catch (e: Exception) {
        null
    }
}

// ✅ GET USER'S NIDS FROM SINGLE FOLDER
fun getUserNIDsFromSingleFolder(context: Context, userId: String): Map<String, List<String>> {
    val singleFolder = File(context.filesDir, "NID_ALL")
    if (!singleFolder.exists()) return emptyMap()

    val allFiles = singleFolder.listFiles() ?: emptyArray()

    // Filter files for this user
    val userFiles = allFiles.filter { it.name.startsWith("${userId}_") }

    // Group by side (front/back)
    return mapOf(
        "front" to userFiles.filter { it.name.contains("_front_") }
            .map { it.absolutePath }
            .sortedDescending(), // Latest first
        "back" to userFiles.filter { it.name.contains("_back_") }
            .map { it.absolutePath }
            .sortedDescending()
    )
}

// ✅ CHECK IF USER HAS UPLOADED NID
fun hasUserUploadedNIDSingleFolder(context: Context, userId: String): Boolean {
    val files = getUserNIDsFromSingleFolder(context, userId)
    return files["front"]?.isNotEmpty() == true &&
            files["back"]?.isNotEmpty() == true
}