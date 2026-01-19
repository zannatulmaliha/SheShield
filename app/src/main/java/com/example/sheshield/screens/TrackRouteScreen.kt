package com.example.sheshield.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.viewmodel.TrackRouteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackRouteScreen(
    onBack: (() -> Unit)? = null,
    viewModel: TrackRouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val defaultLocation = LatLng(23.8103, 90.4125)

    var isMapLoaded by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val isLoading by viewModel.loadingState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasLocationPermission = it }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            currentLocation = userLatLng
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                        }
                    }
            } catch (e: SecurityException) {}
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Track My Route") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6000E9),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Notify Button
                SmallFloatingActionButton(
                    onClick = {
                        if (currentLocation != null) viewModel.notifyContacts(context, currentLocation)
                        else Toast.makeText(context, "Locating...", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = Color(0xFF03DAC5)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Icon(Icons.Default.Send, "Notify")
                }

                // Share Button
                ExtendedFloatingActionButton(
                    onClick = {
                        if (currentLocation != null) {
                            val mapLink = "http://maps.google.com/?q=${currentLocation!!.latitude},${currentLocation!!.longitude}"
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "I'm sharing my live route: $mapLink")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Location"))
                        }
                    },
                    icon = { Icon(Icons.Default.Share, "Share") },
                    text = { Text("Share Location") },
                    containerColor = Color(0xFF6000E9),
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (hasLocationPermission) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapLoaded = { isMapLoaded = true }
                )
            }
            if (!isMapLoaded && hasLocationPermission) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}