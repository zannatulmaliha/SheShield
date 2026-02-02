package com.example.sheshield.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Consistent SheShield Dark Theme Palette
private val BackgroundDark = Color(0xFF1A1A2E)
private val SurfaceCard = Color(0xFF25254A)
private val PrimaryPurple = Color(0xFF8B7FFF)
private val AccentEmerald = Color(0xFF34D399)
private val TextPrimary = Color(0xFFE8E8F0)
private val TextSecondary = Color(0xFFB4B4C8)

@Composable
fun RespondersNearMeScreen(onBackClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { TopBarResponders(onBackClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundDark, Color(0xFF16213E), BackgroundDark)
                    )
                )
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // MAP SECTION
            MapSection()

            // RADIUS SELECTOR
            RadiusSelector()

            // STATS
            StatsRow()

            // HOW IT WORKS
            HowItWorksCard()

            // RESPONDER LIST
            ResponderList()

            // BECOME A HELPER
            BecomeHelperCard()

            Spacer(Modifier.height(24.dp))
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

@Composable
fun StatsRow() {
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