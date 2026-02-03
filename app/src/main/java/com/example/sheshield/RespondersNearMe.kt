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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.core.app.ActivityCompat
import com.example.sheshield.models.UserData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

// --- GLASS GLOW PALETTE ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val StatPink = Color(0xFFFF2D92)

@Composable
fun RespondersNearMeScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var radiusKm by remember { mutableFloatStateOf(10f) }
    var myLocation by remember { mutableStateOf<Location?>(null) }
    val nearbyHelpers = remember { mutableStateListOf<UserData>() }
    var helperCountInDb by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val savedRadius = document.getDouble("searchRadius")?.toFloat()
                        if (savedRadius != null) radiusKm = savedRadius
                    }
                }
        }
    }

    LaunchedEffect(radiusKm) {
        if (userId != null) {
            delay(1000)
            db.collection("users").document(userId).update("searchRadius", radiusKm)
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { loc -> myLocation = loc }
        }
    }

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
                                    if (distance <= radiusKm) nearbyHelpers.add(helper)
                                }
                            } catch (ex: Exception) { Log.e("Responders", "Error parsing", ex) }
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = { TopBarResponders(onBackClick) },
        containerColor = MidnightBase
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MAP SECTION (Framed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            ) {
                if (myLocation != null) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(myLocation!!.latitude, myLocation!!.longitude), 12f
                        )
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(myLocation!!.latitude, myLocation!!.longitude)),
                            title = "You",
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET)
                        )
                        nearbyHelpers.forEach { helper ->
                            val lat = (helper.location?.get("latitude") as? Number)?.toDouble() ?: 0.0
                            val lng = (helper.location?.get("longitude") as? Number)?.toDouble() ?: 0.0
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = helper.name
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(TopBarDeep), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { RadiusSelector(radiusKm) { radiusKm = it } }

                item {
                    StatsRow(
                        available = nearbyHelpers.size.toString(),
                        total = helperCountInDb.toString()
                    )
                }

                item { HowItWorksCard() }

                item {
                    Text(
                        "Responders in Range",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (nearbyHelpers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                            Text("No responders within ${radiusKm.toInt()}km", color = Color.White.copy(0.4f))
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
                        } else "..."
                        ResponderCardReal(helper, distStr, context)
                    }
                }

                item { BecomeHelperCard() }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun TopBarResponders(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase)))
            .padding(top = 45.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick, modifier = Modifier.background(GlassWhite, CircleShape)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Text("Nearby Responders", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RadiusSelector(currentRadius: Float, onRadiusChange: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite, RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Search Radius", color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold)
                Text("${currentRadius.toInt()} km", color = AccentPurple, fontWeight = FontWeight.Black)
            }
            Slider(
                value = currentRadius,
                onValueChange = onRadiusChange,
                valueRange = 1f..50f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = AccentPurple,
                    inactiveTrackColor = GlassWhite
                )
            )
        }
    }
}

@Composable
fun StatsRow(available: String, total: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatBox(available, "Nearby", AccentEmerald)
        StatBox(total, "Active", AccentPurple)
        StatBox("5m", "Avg Time", StatPink)
    }
}

@Composable
fun RowScope.StatBox(value: String, label: String, color: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(GlassWhite, RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 11.sp, color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HowItWorksCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentPurple.copy(0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, AccentPurple.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.SupportAgent, null, tint = AccentPurple, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            "Verified helpers are notified when you trigger SOS. Tracking is real-time.",
            fontSize = 13.sp,
            color = Color.White.copy(0.7f),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ResponderCardReal(helper: UserData, distance: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite, RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(AccentPurple.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(helper.name.take(1), fontWeight = FontWeight.Black, color = AccentPurple, fontSize = 20.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(helper.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(distance, color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        IconButton(
            onClick = {
                if (helper.phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${helper.phone}") }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No phone number", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.background(AccentEmerald, CircleShape).size(44.dp)
        ) {
            Icon(Icons.Default.Call, "Call", tint = Color.Black, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun BecomeHelperCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(TopBarDeep, Color(0xFF1B3B2B))), RoundedCornerShape(24.dp))
            .border(1.dp, AccentEmerald.copy(0.3f), RoundedCornerShape(24.dp))
            .clickable { }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Be a Hero", color = AccentEmerald, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Apply to join our responder network.", color = Color.White.copy(0.6f), fontSize = 12.sp)
            }
            Icon(Icons.Default.Shield, null, tint = AccentEmerald, modifier = Modifier.size(40.dp))
        }
    }
}