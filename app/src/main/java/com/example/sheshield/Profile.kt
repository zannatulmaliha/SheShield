package com.example.sheshield

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ===== TRUE Dark Purple SheShield Theme ===== */
/* ===== DARKER TRUE SHESHIELD THEME ===== */
private val BackgroundDark = Color(0xFF151029)       // darker background
private val SurfaceCard = Color(0xFF221B3F)         // darker card
private val HeaderPurple = Color(0xFF2C2452)        // deeper header
private val AvatarBg = Color(0xFF3F3380)            // darker avatar bg
private val PrimaryPurple = Color(0xFF7F6BFF)       // slightly muted primary
private val AccentEmerald = Color(0xFF39FF99)       // vibrant green
private val DangerRose = Color(0xFFFF5C75)          // darker rose for danger
private val TextPrimary = Color(0xFFEDECF9)         // softer white
private val TextSecondary = Color(0xFFBEB6E3)       // softer secondary text
private val DividerColor = Color.White.copy(alpha = 0.08f) //

@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()

    Surface(color = BackgroundDark, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 30.dp)
        ) {
            profile_settings_header()

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionHeader("PERSONAL INFORMATION")
                DarkPurpleCard {
                    InfoRow(Icons.Default.Email, "Email", "bob@gmail.com")
                    HorizontalDivider(color = DividerColor)
                    InfoRow(Icons.Default.Phone, "Phone Number", "Tap to add phone")
                    HorizontalDivider(color = DividerColor)
                    InfoRow(Icons.Default.LocationOn, "Home Address", "Tap to add address")
                }

                SectionHeader("SAFETY SETTINGS")
                DarkPurpleCard {
                    InfoRow(Icons.Default.Warning, "SOS Triggers", "Shake, voice, power button")
                    HorizontalDivider(color = DividerColor)
                    InfoRow(Icons.Default.Group, "Trusted Contacts", "3 contacts added")
                }

                SectionHeader("PRIVACY & SECURITY")
                DarkPurpleCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = PrimaryPurple)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("App Lock", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Require PIN or biometric",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AccentEmerald,
                                checkedThumbColor = Color.White
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentEmerald.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = AccentEmerald)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Verified User",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRose.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DangerRose),
                    onClick = {}
                ) {
                    Text("Log Out", color = DangerRose, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun profile_settings_header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderPurple)
            .padding(top = 40.dp, bottom = 10.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            "Profile & Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DividerColor, RoundedCornerShape(20.dp)),
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = AvatarBg
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(AccentEmerald, CircleShape)
                            .border(2.dp, SurfaceCard, CircleShape)
                            .align(Alignment.TopStart)
                            .offset(x = 4.dp, y = 4.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("bob", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row {
                        Text("Safety Status: ", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "Active",
                            color = AccentEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.10f)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextSecondary, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = TextSecondary)
    }
}

@Composable
fun DarkPurpleCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        content = content
    )
}
