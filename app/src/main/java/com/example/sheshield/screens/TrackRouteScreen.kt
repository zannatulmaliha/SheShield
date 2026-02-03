package com.example.sheshield.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheshield.viewmodel.TrackRouteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// --- GLASS GLOW PALETTE ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)

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

    Box(modifier = Modifier.fillMaxSize().background(MidnightBase)) {
        Scaffold(
            containerColor = Color.Transparent, // Let the background show through
            topBar = {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase.copy(0.8f))))
                            .padding(top = 45.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.background(GlassWhite, CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "Track My Route",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    // Quick Notify Button (Glass Emerald)
                    FloatingActionButton(
                        onClick = {
                            if (currentLocation != null) viewModel.notifyContacts(context, currentLocation)
                            else Toast.makeText(context, "Locating...", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = AccentEmerald,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Send, "Notify")
                    }

                    // Share Button (Neon Purple)
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
                        text = { Text("Share Location", fontWeight = FontWeight.Bold) },
                        containerColor = AccentPurple,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MidnightBase)
            ) {
                // Map Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                ) {
                    if (hasLocationPermission) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false),
                            onMapLoaded = { isMapLoaded = true }
                        )
                    }
                }

                // Glass Loading Overlay
                if (!isMapLoaded && hasLocationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MidnightBase.copy(0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
            }
        }
    }
}