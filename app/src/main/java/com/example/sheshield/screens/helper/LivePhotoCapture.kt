

package com.example.sheshield.screens.helper

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.util.concurrent.Executors

@Composable
fun LivePhotoCapture(
    onResult: (success: Boolean, file: File?) -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Camera permission required")
        }
        return
    }

    //---------------------------------------------------
    // Stable camera objects
    //---------------------------------------------------

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // VERY IMPORTANT → background thread
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ML Kit detector (created ONCE)
    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
    }

    //---------------------------------------------------
    // Camera setup (runs ONLY once)
    //---------------------------------------------------

    LaunchedEffect(previewView) {

        val cameraProvider =
            ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageCapture = capture

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                capture
            )

        } catch (e: Exception) {
            Log.e("LivePhoto", "Camera binding failed: ${e.message}")
        }
    }

    //---------------------------------------------------
    // UI
    //---------------------------------------------------


    Box(modifier = Modifier.fillMaxSize()) {

        // Camera Preview (FULL SCREEN)
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Status text (top)
        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )
        }

        // Capture Button (floating bottom center)
        Button(
            onClick = { /* KEEP YOUR EXISTING CLICK CODE */
                val capture = imageCapture ?: return@Button

                isProcessing = true
                statusMessage = "Processing..."

                val photoFile =
                    File(context.cacheDir, "live_photo.jpg")

                val outputOptions =
                    ImageCapture.OutputFileOptions.Builder(photoFile).build()

                capture.takePicture(
                    outputOptions,
                    cameraExecutor, // background thread!
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onError(exc: ImageCaptureException) {
                            Log.e("LivePhoto", "Capture error ${exc.message}")

                            isProcessing = false
                            statusMessage = "Capture failed"
                            onResult(false, null)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                            Log.d("LivePhoto", "Image saved")

                            try {

                                // Resize bitmap to prevent ML freeze
                                val options = BitmapFactory.Options().apply {
                                    inSampleSize = 2
                                }

                                val bitmap = BitmapFactory.decodeFile(
                                    photoFile.absolutePath,
                                    options
                                )

                                val image = InputImage.fromBitmap(bitmap, 0)

                                detector.process(image)
                                    .addOnSuccessListener { faces ->

                                        Log.d("LivePhoto", "Faces: ${faces.size}")

                                        isProcessing = false

                                        if (faces.isNotEmpty()) {
                                            statusMessage = "✅ Face detected"
                                            onResult(true, photoFile)
                                        } else {
                                            statusMessage = "❌ No face detected"
                                            onResult(false, photoFile)
                                        }
                                    }
                                    .addOnFailureListener {

                                        Log.e("LivePhoto", "MLKit failed")

                                        isProcessing = false
                                        statusMessage = "Detection failed"
                                        onResult(false, photoFile)
                                    }

                            } catch (e: Exception) {

                                Log.e("LivePhoto", "Processing crash ${e.message}")

                                isProcessing = false
                                statusMessage = "Processing error"
                                onResult(false, photoFile)
                            }
                        }
                    }
                )

            },
            enabled = !isProcessing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .fillMaxWidth(0.75f)
                .height(60.dp)
        ) {
            Text(if (isProcessing) "Processing..." else "Take Photo")
        }
    }

    //---------------------------------------------------
    // Cleanup (VERY IMPORTANT)
    //---------------------------------------------------

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
