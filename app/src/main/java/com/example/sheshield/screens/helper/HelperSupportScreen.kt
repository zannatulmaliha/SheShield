package com.example.sheshield.screens.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FIXED: Removed 'private' so public functions can use it
enum class SupportView {
    MAIN, SAFETY_PROTOCOLS, REPORT_ISSUE, GUIDELINES, FAQ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperSupportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentView by remember { mutableStateOf(SupportView.MAIN) }
    var reportCategory by remember { mutableStateOf("") } // To pass data to Report screen

    // Handle back button press
    BackHandler(enabled = currentView != SupportView.MAIN) {
        currentView = SupportView.MAIN
    }

    // Main layout container
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentView) {
                            SupportView.MAIN -> "Help & Support"
                            SupportView.SAFETY_PROTOCOLS -> "Safety Protocols"
                            SupportView.REPORT_ISSUE -> "Report an Issue"
                            SupportView.GUIDELINES -> "Helper Guidelines"
                            SupportView.FAQ -> "Frequently Asked Questions"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentView == SupportView.MAIN) onBack() else currentView = SupportView.MAIN
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9333EA)
                )
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentView) {
                SupportView.MAIN -> SupportMainContent(
                    context = context,
                    onNavigate = { view -> currentView = view },
                    onQuickReport = { category ->
                        reportCategory = category
                        currentView = SupportView.REPORT_ISSUE
                    }
                )
                SupportView.SAFETY_PROTOCOLS -> SafetyProtocolsContent()
                SupportView.REPORT_ISSUE -> ReportIssueContent(initialCategory = reportCategory, onSubmitted = { currentView = SupportView.MAIN })
                SupportView.GUIDELINES -> GuidelinesContent()
                SupportView.FAQ -> FaqContent()
            }
        }
    }
}

// ----------------------------------------------------------------
// 1. MAIN MENU CONTENT
// ----------------------------------------------------------------
@Composable
fun SupportMainContent(
    context: Context,
    onNavigate: (SupportView) -> Unit,
    onQuickReport: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val purplePrimary = Color(0xFF9333EA)
    val redBg = Color(0xFFFEF2F2)
    val redBorder = Color(0xFFFECACA)
    val redText = Color(0xFF991B1B)
    val redButton = Color(0xFFDC2626)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- EMERGENCY HOTLINE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = redBg),
            border = BorderStroke(1.dp, redBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(redButton, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Emergency Hotline", fontWeight = FontWeight.Bold, color = redText, fontSize = 16.sp)
                    Text(
                        "Available 24/7 for urgent situations during active responses",
                        style = MaterialTheme.typography.bodySmall,
                        color = redText.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:999") }
                            try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = redButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call 999")
                    }
                }
            }
        }

        // --- QUICK ACCESS ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Quick Access", color = Color.Gray, fontSize = 14.sp)

            QuickAccessCard("Safety Protocols", "Essential guidelines for helpers", Icons.Default.Shield, Color(0xFFF3E8FF), purplePrimary) {
                onNavigate(SupportView.SAFETY_PROTOCOLS)
            }
            QuickAccessCard("Report an Issue", "During or after a response", Icons.Default.Report, Color(0xFFDBEAFE), Color(0xFF2563EB)) {
                onNavigate(SupportView.REPORT_ISSUE)
            }
            QuickAccessCard("Helper Guidelines", "Complete helper handbook", Icons.Default.Description, Color(0xFFDCFCE7), Color(0xFF16A34A)) {
                onNavigate(SupportView.GUIDELINES)
            }
            QuickAccessCard("FAQs", "Common questions answered", Icons.Default.HelpOutline, Color(0xFFFFEDD5), Color(0xFFEA580C)) {
                onNavigate(SupportView.FAQ)
            }
        }

        // --- QUICK REPORT ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Quick Report", color = Color.Gray, fontSize = 14.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Choose a category to quickly report an issue:", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickReportButton("Safety Concern", Icons.Default.Warning, Color(0xFFEA580C), Modifier.weight(1f)) { onQuickReport("Safety Concern") }
                        QuickReportButton("False Alert", Icons.Default.Shield, purplePrimary, Modifier.weight(1f)) { onQuickReport("False Alert") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickReportButton("User Behavior", Icons.AutoMirrored.Filled.Message, Color(0xFF2563EB), Modifier.weight(1f)) { onQuickReport("User Behavior") }
                        QuickReportButton("Other Issue", Icons.Default.Help, Color.Gray, Modifier.weight(1f)) { onQuickReport("Other") }
                    }
                }
            }
        }

        // --- CONTACT SUPPORT ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Contact Support", color = Color.Gray, fontSize = 14.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SupportContactRow(Icons.Default.Email, "Email Support", "helper-support@sheshield.com") {
                        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:helper-support@sheshield.com") }
                        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() }
                    }
                    SupportContactRow(Icons.AutoMirrored.Filled.Chat, "Live Chat", "Available 9 AM - 9 PM") {
                        Toast.makeText(context, "Connecting to agent...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // --- LEGAL ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Legal & Privacy", color = Color.Gray, fontSize = 14.sp)
            LegalItem("Helper Terms of Service") { openUrl(context, "https://example.com/terms") }
            LegalItem("Privacy Policy") { openUrl(context, "https://example.com/privacy") }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ----------------------------------------------------------------
// 2. SAFETY PROTOCOLS SCREEN
// ----------------------------------------------------------------
@Composable
fun SafetyProtocolsContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Before Accepting", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF9333EA))
                Spacer(modifier = Modifier.height(8.dp))
                CheckListItem("Check your surroundings are safe")
                CheckListItem("Ensure you can reach the location safely")
                CheckListItem("Inform your emergency contact")
                CheckListItem("Keep your phone charged")

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text("During Response", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF9333EA))
                Spacer(modifier = Modifier.height(8.dp))
                CheckListItem("Stay in well-lit, public areas")
                CheckListItem("Maintain communication via the app")
                CheckListItem("Do not enter private residences alone")
                CheckListItem("If user is unconscious, call 999 immediately")

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Red Flags (Abort Mission)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                WarningListItem("Aggressive or threatening behavior")
                WarningListItem("Request to meet in isolated/dark area")
                WarningListItem("User asks for money or personal info")
            }
        }
    }
}

// ----------------------------------------------------------------
// 3. REPORT ISSUE SCREEN
// ----------------------------------------------------------------
@Composable
fun ReportIssueContent(initialCategory: String, onSubmitted: () -> Unit) {
    var category by remember { mutableStateOf(if (initialCategory.isNotEmpty()) initialCategory else "Safety Concern") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Report an Issue", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Category", fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = category == "Safety Concern", onClick = { category = "Safety Concern" }, label = { Text("Safety") })
            FilterChip(selected = category == "False Alert", onClick = { category = "False Alert" }, label = { Text("False Alert") })
            FilterChip(selected = category == "User Behavior", onClick = { category = "User Behavior" }, label = { Text("User") })
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe the issue details") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                Toast.makeText(context, "Report submitted. ID: #${(1000..9999).random()}", Toast.LENGTH_LONG).show()
                onSubmitted()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
        ) {
            Text("Submit Report")
        }
    }
}

// ----------------------------------------------------------------
// 4. GUIDELINES SCREEN
// ----------------------------------------------------------------
@Composable
fun GuidelinesContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Helper Community Guidelines", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "1. Professionalism\nHelpers must maintain a professional demeanor. Do not engage in personal arguments or make judgmental comments.\n\n" +
                    "2. Privacy\nNever share a user's location or personal details on social media. Confidentiality is paramount.\n\n" +
                    "3. No Vigilantism\nHelpers are NOT police. Your role is support and de-escalation. If a crime is in progress, call authorities.\n\n" +
                    "4. Zero Tolerance\nHarassment, discrimination, or predatory behavior results in an immediate ban."
            , lineHeight = 24.sp, color = Color.DarkGray)
    }
}

// ----------------------------------------------------------------
// 5. FAQ SCREEN
// ----------------------------------------------------------------
@Composable
fun FaqContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        FaqItem("What if I feel unsafe?", "Leave the area immediately and call 999. Cancel the alert in the app afterwards.")
        FaqItem("Do I get paid?", "SheShield is currently a volunteer network. We offer badges and community recognition.")
        FaqItem("Can I turn off alerts?", "Yes, toggle the 'Active' switch on your dashboard.")
        FaqItem("What constitutes a 'False Alert'?", "If you arrive and the user says they didn't mean to trigger it, or if no one is there.")
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(question, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(answer, color = Color.Gray)
            }
        }
    }
}

// --- HELPER COMPONENTS ---

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun QuickAccessCard(title: String, subtitle: String, icon: ImageVector, iconBg: Color, iconColor: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun CheckListItem(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
        Text("✓", color = Color(0xFF16A34A), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF374151))
    }
}

@Composable
fun WarningListItem(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
        Text("⚠", color = Color(0xFFDC2626), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF991B1B))
    }
}

@Composable
fun SupportContactRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = Color(0xFF9333EA), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(title, color = Color(0xFF1F2937), fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun QuickReportButton(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = Color(0xFF374151))
        }
    }
}

@Composable
fun LegalItem(title: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 14.sp, color = Color(0xFF1F2937))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}