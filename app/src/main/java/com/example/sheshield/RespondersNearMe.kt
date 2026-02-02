package com.example.sheshield.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.sheshield.models.UserData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

// Consistent SheShield Dark Theme Palette
private val BackgroundDark = Color(0xFF1A1A2E)
private val SurfaceCard = Color(0xFF25254A)
private val PrimaryPurple = Color(0xFF8B7FFF)
private val AccentEmerald = Color(0xFF34D399)
private val TextPrimary = Color(0xFFE8E8F0)
private val TextSecondary = Color(0xFFB4B4C8)

@Composable
fun RespondersNearMeScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // --- STATES ---
    var radiusKm by remember { mutableFloatStateOf(10f) } // Default 10km
    var myLocation by remember { mutableStateOf<Location?>(null) }
    val nearbyHelpers = remember { mutableStateListOf<UserData>() }
    var helperCountInDb by remember { mutableIntStateOf(0) }

    // --- 1. SYNC RADIUS FROM FIRESTORE (Persistence Fix) ---
    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Retrieve saved "searchRadius"
                        val savedRadius = document.getDouble("searchRadius")?.toFloat()
                        if (savedRadius != null) {
                            radiusKm = savedRadius
                        }
                    }
                }
        }
    }

    // --- 2. AUTO-SAVE RADIUS TO FIRESTORE ---
    LaunchedEffect(radiusKm) {
        if (userId != null) {
            delay(1000) // Debounce: wait 1 sec after sliding stops
            db.collection("users").document(userId)
                .update("searchRadius", radiusKm)
        }
    }

    // 3. GET USER LOCATION
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { loc ->
                    myLocation = loc
                    Log.d("Responders", "User Location: ${loc?.latitude}, ${loc?.longitude}")
                }
        }
    }

    // 4. QUERY FIRESTORE FOR ACTIVE HELPERS
    LaunchedEffect(myLocation, radiusKm) {
        if (myLocation != null) {
            db.collection("users")
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener

                    nearbyHelpers.clear()
                    if (snapshots != null) {
                        helperCountInDb = snapshots.size()

                        for (doc in snapshots) {
                            try {
                                val helper = doc.toObject(UserData::class.java)
                                val locMap = helper.location

                                if (locMap != null) {
                                    val hLoc = Location("helper")
                                    val lat = (locMap["latitude"] as? Number)?.toDouble() ?: 0.0
                                    val lng = (locMap["longitude"] as? Number)?.toDouble() ?: 0.0

                                    hLoc.latitude = lat
                                    hLoc.longitude = lng

                                    val distance = myLocation!!.distanceTo(hLoc) / 1000

                                    // Add to list if within radius
                                    if (distance <= radiusKm) {
                                        nearbyHelpers.add(helper)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("Responders", "Error parsing helper", e)
                            }
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = { TopBarResponders(onBackClick) },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MAP SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFEAF0FF))
            ) {
                if (myLocation != null) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(myLocation!!.latitude, myLocation!!.longitude), 13f
                        )
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
                    ) {
                        // User Marker
                        Marker(
                            state = MarkerState(position = LatLng(myLocation!!.latitude, myLocation!!.longitude)),
                            title = "You",
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                        )

                        // Helper Markers
                        nearbyHelpers.forEach { helper ->
                            val lat = (helper.location?.get("latitude") as? Number)?.toDouble() ?: 0.0
                            val lng = (helper.location?.get("longitude") as? Number)?.toDouble() ?: 0.0
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = helper.name,
                                snippet = "Tap card below to call"
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6000E9))
                        Text("Locating you...", Modifier.padding(top = 40.dp), color = Color.Gray)
                    }
                }
            }

            // LIST SECTION
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    RadiusSelector(radiusKm) { newRadius -> radiusKm = newRadius }
                }

                item {
                    StatsRow(
                        available = nearbyHelpers.size.toString(),
                        total = helperCountInDb.toString()
                    )
                }

                item { HowItWorksCard() }

                item {
                    Text("Nearby Responders (${nearbyHelpers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                if (nearbyHelpers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No active responders found within ${radiusKm.toInt()}km.", color = Color.Gray)
                        }
                    }
                } else {
                    items(nearbyHelpers) { helper ->
                        val distStr = if (myLocation != null && helper.location != null) {
                            val lat = (helper.location["latitude"] as? Number)?.toDouble() ?: 0.0
                            val lng = (helper.location["longitude"] as? Number)?.toDouble() ?: 0.0
                            val hLoc = Location("").apply { latitude = lat; longitude = lng }
                            val d = myLocation!!.distanceTo(hLoc) / 1000
                            String.format("%.1f km", d)
                        } else "Unknown"

                        ResponderCardReal(helper, distStr, context)
                    }
                }

                item { BecomeHelperCard() }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarResponders(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4C3F8F), Color(0xFF3A2F6F))
                )
            )
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Responders Near Me",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    }
}

@Composable
fun MapSection() {
    val centerLocation = LatLng(23.8103, 90.4125)
    val responderLocations = listOf(
        LatLng(23.8123, 90.4145),
        LatLng(23.8083, 90.4105),
        LatLng(23.8143, 90.4100),
        LatLng(23.8090, 90.4160)
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 14.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = centerLocation),
                title = "You",
                snippet = "Current Location"
            )
            responderLocations.forEachIndexed { index, latLng ->
                MarkerComposable(
                    state = MarkerState(position = latLng),
                    title = "Responder ${index + 1}"
                ) {
                    ResponderMapMarker(index)
                }
            }
            Text("Responders Near Me", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResponderMapMarker(index: Int) {
    val colors = listOf(AccentEmerald, Color(0xFF6B5FEE), Color(0xFFFBBF24), Color(0xFFFB7185))
    val color = colors[index % colors.size]

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(Color.White, CircleShape)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "R${index + 1}",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RadiusSelector() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Search Radius", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                var radius by remember { mutableFloatStateOf(2f) }
                Text("${radius.toInt()} km", color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }

            var radius by remember { mutableFloatStateOf(2f) }
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 1f..5f,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryPurple,
                    activeTrackColor = PrimaryPurple,
                    inactiveTrackColor = PrimaryPurple.copy(alpha = 0.2f)
                )
            )
        }
    }
}

// ---------------- STATS (Corrected) ----------------

@Composable
fun StatsRow(available: String, total: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox("4", "Available", AccentEmerald)
        StatBox("5", "Total Nearby", PrimaryPurple)
        StatBox("< 5m", "Response", Color(0xFFFB7185))
    }
}

@Composable
fun RowScope.StatBox(value: String, label: String, accentColor: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(label, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun HowItWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("How It Works", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "When SOS is triggered, verified helpers nearby are notified to assist. All responders are background-checked.",
                    fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ResponderList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nearby Responders", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        ResponderCard("MD Shirajul Islam", "0.5 km", "3–5 min", "Available")
        ResponderCard("Nilufar Yasmin", "0.7 km", "4–6 min", "Available")
        ResponderCard("AK Himel", "1.2 km", "5–8 min", "Busy")
    }
}

@Composable
fun ResponderCard(name: String, distance: String, time: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (name.contains(" ")) "${name.first()}${name.split(" ").last().first()}" else name.take(2)
                Text(initials, color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("$distance • $time", fontSize = 12.sp, color = TextSecondary)
            }

            StatusTag(status)
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val isAvailable = status == "Available"
    Box(
        modifier = Modifier
            .background(
                if (isAvailable) AccentEmerald.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            status,
            fontSize = 11.sp,
            color = if (isAvailable) AccentEmerald else Color(0xFFFB7185),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BecomeHelperCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentEmerald)
                Spacer(Modifier.width(10.dp))
                Text("Want to Help Others?", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Join our verified helper network and make a difference.", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply to be a Helper", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}