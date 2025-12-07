package com.example.sheshield

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.ui.theme.SheShieldTheme

@Composable
fun RespondersNearMeScreen() {
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
            TopBarResponders()

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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
fun TopBarResponders() {
    Surface(
        color = Color(0xFF7831A4),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 15.dp, bottomEnd = 15.dp),
        modifier = Modifier.padding(top = 30.dp).fillMaxWidth(1f)
    ) {
        Column(Modifier.padding(all = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "Responders Near Me",
                        Modifier.padding(bottom = 5.dp),
                        color = Color.White,
                        fontSize = 25.sp
                    )
                }

            }
        }
    }
}

@Composable
fun MapSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(Color(0xFFEAF0FF), RoundedCornerShape(20.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF7B2CFF),
            modifier = Modifier.size(50.dp)
        )
    }
}

// ---------------- RADIUS SLIDER ----------------

@Composable
fun RadiusSelector() {
    Column {
        Text("Search Radius", fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))

        var radius by remember { mutableStateOf(2f) }

        Slider(
            value = radius,
            onValueChange = { radius = it },
            valueRange = 1f..5f,
            modifier = Modifier.fillMaxWidth()
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
        StatBox("4", "Available", Color(0xFFD9FBE5))
        StatBox("5", "Total Nearby", Color(0xFFEAF0FF))
        StatBox("< 5 min", "Response", Color(0xFFFFE3F3))
    }
}

@Composable
fun StatBox(value: String, label: String, bg: Color) {
    Column(
        modifier = Modifier
            //.weight(1f)
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp)
        Text(label, fontSize = 13.sp, color = Color.Gray)
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
        Text("How It Works", fontSize = 16.sp, color = Color(0xFF7B2CFF))
        Spacer(Modifier.height(6.dp))
        Text(
            "When you activate SOS, verified helpers nearby will be notified " +
                    "and can choose to respond. All helpers are verified and rated.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// ---------------- RESPONDER LIST ----------------

@Composable
fun ResponderList() {
    Column {
        Text("Nearby Responders", fontSize = 18.sp)
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
            .border(1.dp, Color.LightGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEE6FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.split(" ").first()[0].toString() +
                        name.split(" ").last()[0],
                fontSize = 16.sp,
                color = Color(0xFF6B20D8)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("$distance • $time", fontSize = 12.sp, color = Color.Gray)
        }

        StatusTag(status)
    }
}

@Composable
fun StatusTag(status: String) {
    val color = when (status) {
        "Available" -> Color(0xFFB6F9C9)
        "Busy" -> Color(0xFFFFD4D4)
        else -> Color.LightGray
    }
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, fontSize = 12.sp)
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
            .padding(12.dp)
    ) {
        Text("Want to Help Others?", fontSize = 16.sp)
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
                .background(Color(0xFF54D56B), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Apply to be a Helper", color = Color.White)
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