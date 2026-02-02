package com.example.sheshield

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// ---------------- HEADER ----------------

@Composable
fun TopBarResponders(onBackClick: () -> Unit) {
    Surface(
        color = Color(0xFF6000E9),
        shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 40.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text("Responders Near Me", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- RADIUS SLIDER ----------------

@Composable
fun RadiusSelector(currentRadius: Float, onRadiusChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Search Radius", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${currentRadius.toInt()} km", fontSize = 14.sp, color = Color(0xFF6000E9), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))

        Slider(
            value = currentRadius,
            onValueChange = onRadiusChange,
            valueRange = 1f..50f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF6000E9), activeTrackColor = Color(0xFF6000E9)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------- STATS (Corrected) ----------------

@Composable
fun StatsRow(available: String, total: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        StatBox(available, "Nearby", Color(0xFFD9FBE5))
        StatBox(total, "Total Active", Color(0xFFEAF0FF))
        StatBox("5 min", "Avg Time", Color(0xFFFFE3F3))
    }
}

@Composable
fun RowScope.StatBox(value: String, label: String, bg: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

// ---------------- HOW IT WORKS ----------------

@Composable
fun HowItWorksCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(15.dp))
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Text("How It Works", fontSize = 16.sp, color = Color(0xFF6000E9), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "When you activate SOS, verified helpers nearby will be notified. Icons on the map update in real-time.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// ---------------- RESPONDER CARD ----------------

@Composable
fun ResponderCardReal(helper: UserData, distance: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEE6FF)),
            contentAlignment = Alignment.Center
        ) {
            val initials = if (helper.name.isNotBlank()) helper.name.take(1) else "?"
            Text(initials, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6000E9))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(helper.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("$distance away", fontSize = 13.sp, color = Color.Gray)
        }

        IconButton(
            onClick = {
                if (helper.phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${helper.phone}")
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No phone number", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .background(Color(0xFFE8F5E9), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Call, "Call", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun BecomeHelperCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE9FFE7), RoundedCornerShape(15.dp))
            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Text("Want to Help Others?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Join our verified helper network.", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                .clickable { }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Apply to be a Helper", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}