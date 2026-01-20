package com.example.sheshield

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.ui.theme.SheShieldTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun RespondersNearMeScreen(onBackClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFFF6F7FB),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 35.dp)
        ) {
            // Pass the back click event to the Top Bar
            TopBarResponders(onBackClick)

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // REPLACED THE PLACEHOLDER WITH THE ACTUAL MAP
                MapSection()

                RadiusSelector()

                StatsRow()

                HowItWorksCard()

                ResponderList()

                BecomeHelperCard()

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------- HEADER ----------------

@Composable
fun TopBarResponders(onBackClick: () -> Unit) {
    Surface(
        color = Color(0xFF6000E9),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 15.dp,
            bottomEnd = 15.dp
        ),
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(all = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Responders Near Me",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------- MAP SECTION (NEW) ----------------

@Composable
fun MapSection() {
    // 1. Define a center point (e.g., Dhaka/Current Location)
    val centerLocation = LatLng(23.8103, 90.4125)

    // 2. Define fake locations for responders around the center
    val responderLocations = listOf(
        LatLng(23.8123, 90.4145), // North East
        LatLng(23.8083, 90.4105), // South West
        LatLng(23.8143, 90.4100), // North West
        LatLng(23.8090, 90.4160)  // South East
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 14.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Increased height slightly for better view
            .clip(RoundedCornerShape(20.dp)) // Clips the map to rounded corners
            .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
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
            // Marker for "You" (Center)
            Marker(
                state = MarkerState(position = centerLocation),
                title = "You",
                snippet = "Current Location"
            )

            // Custom Profile Markers for Responders
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

// Custom UI for the Map Marker (Looks like a profile bubble)
@Composable
fun ResponderMapMarker(index: Int) {
    val colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFF9C27B0))
    val color = colors[index % colors.size]

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color.White, CircleShape)
            .padding(3.dp) // White border effect
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "R${index + 1}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ---------------- RADIUS SLIDER ----------------

@Composable
fun RadiusSelector() {
    Column {
        Text("Search Radius", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        var radius by remember { mutableFloatStateOf(2f) }

        Slider(
            value = radius,
            onValueChange = { radius = it },
            valueRange = 1f..5f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6000E9),
                activeTrackColor = Color(0xFF6000E9)
            )
        )
        Text("${radius.toInt()} km", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Box(Modifier.weight(1f)) { StatBox("4", "Available", Color(0xFFD9FBE5)) }
        Box(Modifier.weight(1f)) { StatBox("5", "Total Nearby", Color(0xFFEAF0FF)) }
        Box(Modifier.weight(1f)) { StatBox("< 5 min", "Response", Color(0xFFFFE3F3)) }
    }
}

@Composable
fun StatBox(value: String, label: String, bg: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
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
            .padding(16.dp)
    ) {
        Text(
            "How It Works",
            fontSize = 16.sp,
            color = Color(0xFF7B2CFF),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "When you activate SOS, verified helpers nearby will be notified " +
                    "and can choose to respond. All helpers are verified and rated.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

// ---------------- RESPONDER LIST ----------------

@Composable
fun ResponderList() {
    Column {
        Text("Nearby Responders", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ResponderCard("MD Shirajul Islam", "0.5 km", "3–5 min", "Available")
        Spacer(Modifier.height(12.dp))
        ResponderCard("Nilufar Yasmin", "0.7 km", "4–6 min", "Available")
        Spacer(Modifier.height(12.dp))
        ResponderCard("AK Himel", "1.2 km", "5–8 min", "Busy")
        Spacer(Modifier.height(12.dp))
        ResponderCard("Mahmud", "1.5 km", "6–10 min", "Available")
    }
}

@Composable
fun ResponderCard(name: String, distance: String, time: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
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
            val initials = if (name.contains(" ")) {
                "${name.first()}${name.split(" ").last().first()}"
            } else {
                name.take(2)
            }
            Text(
                text = initials,
                fontSize = 16.sp,
                color = Color(0xFF6B20D8),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("$distance • $time", fontSize = 12.sp, color = Color.Gray)
        }

        StatusTag(status)
    }
}

@Composable
fun StatusTag(status: String) {
    val (bgColor, textColor) = when (status) {
        "Available" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Busy" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color.Gray
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

// ---------------- BECOME A HELPER ----------------

@Composable
fun BecomeHelperCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE9FFE7), RoundedCornerShape(15.dp))
            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(15.dp))
            .padding(16.dp)
    ) {
        Text("Want to Help Others?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Join our verified helper network and make a difference.",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                .padding(12.dp)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Apply to be a Helper",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResponders() {
    SheShieldTheme {
        RespondersNearMeScreen()
    }
}