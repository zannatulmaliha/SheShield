package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// --- Midnight Palette ---
private val MidnightBase = Color(0xFF0B0F1A)
private val TopBarDeep = Color(0xFF1E1B4B)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.08f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val DangerRose = Color(0xFFFB7185)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(
    onLoginSuccess: () -> Unit,
    onSwitchToHelperMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("user") }
    var gender by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val isMale = gender.lowercase() == "male"
    LaunchedEffect(gender) {
        if (isMale && !isLoginMode) {
            userType = "helper"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBase)
    ) {
        // 1. Header with Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(Brush.verticalGradient(listOf(TopBarDeep, MidnightBase))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = AccentPurple.copy(0.2f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .size(90.dp)
                        .border(1.dp, AccentPurple.copy(0.5f), RoundedCornerShape(24.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "App Icon",
                            modifier = Modifier.size(45.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "SheShield",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = if (isLoginMode) "WELCOME BACK" else "JOIN THE SHIELD",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 2. Form content - Glassmorphic Card
        Surface(
            color = Color.White.copy(0.05f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.BottomCenter)
                .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .padding(top = 40.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status message box
                if (statusMessage.isNotEmpty()) {
                    Surface(
                        color = if (statusMessage.startsWith("✅")) AccentEmerald.copy(0.1f) else DangerRose.copy(0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, if (statusMessage.startsWith("✅")) AccentEmerald.copy(0.4f) else DangerRose.copy(0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = statusMessage,
                            color = if (statusMessage.startsWith("✅")) AccentEmerald else DangerRose,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = AccentPurple) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = Color.White.copy(0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = AccentPurple) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = Color.White.copy(0.4f)
                    )
                )

                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "GENDER IDENTITY",
                        color = Color.White.copy(0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenderOption(
                            label = "Female",
                            isSelected = gender == "female",
                            onClick = {
                                gender = "female"
                                if (userType == "helper") userType = "user"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GenderOption(
                            label = "Male",
                            isSelected = gender == "male",
                            onClick = { gender = "male"; userType = "helper" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isMale && gender.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "ACCOUNT TYPE",
                            color = Color.White.copy(0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 10.dp)
                        )

                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        ) {
                            Column {
                                UserTypeRow("User (Need Help)", Icons.Default.Person, userType == "user") { userType = "user" }
                                Divider(color = GlassBorder, thickness = 1.dp)
                                UserTypeRow("Helper Only", Icons.Default.SupervisorAccount, userType == "helper") { userType = "helper" }
                                Divider(color = GlassBorder, thickness = 1.dp)
                                UserTypeRow("Both Roles", Icons.Default.SwitchAccount, userType == "user_helper") { userType = "user_helper" }
                            }
                        }
                    } else if (isMale && gender.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            color = AccentPurple.copy(0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, AccentPurple.copy(0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Male users register as Helpers only.", color = Color.White.copy(0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) { statusMessage = "❌ Please fill all fields"; return@Button }
                        if (!isLoginMode && gender.isEmpty()) { statusMessage = "❌ Please select gender"; return@Button }
                        if (!isLoginMode && !isMale && userType.isEmpty()) { statusMessage = "❌ Please select user type"; return@Button }

                        isLoading = true
                        if (isLoginMode) {
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) { statusMessage = "✅ Success!"; onLoginSuccess() }
                                else { statusMessage = "❌ Failed: ${task.exception?.message}" }
                            }
                        } else {
                            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid ?: ""
                                    val userData = hashMapOf(
                                        "email" to email, "userType" to userType, "gender" to gender,
                                        "name" to email.substringBefore("@"), "phone" to "", "address" to "",
                                        "isHelperVerified" to false, "helperLevel" to 0,
                                        "createdAt" to System.currentTimeMillis(), "updatedAt" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(userId).set(userData).addOnSuccessListener {
                                        isLoading = false; statusMessage = "✅ Account created!"; isLoginMode = true
                                        password = ""; gender = ""; userType = "user"
                                    }.addOnFailureListener { isLoading = false; statusMessage = "❌ Data error" }
                                } else { isLoading = false; statusMessage = "❌ Signup failed" }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    Text(if (isLoginMode) "LOGIN" else "CREATE ACCOUNT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isLoginMode = !isLoginMode; statusMessage = ""; password = ""; gender = ""; userType = "user" }) {
                    Text(if (isLoginMode) "New here? Create Account" else "Already shielded? Login", color = Color.White.copy(0.6f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        isLoading = true
                        db.collection("test").document("compose_test").set(hashMapOf("test" to "ok", "time" to System.currentTimeMillis()))
                            .addOnSuccessListener { isLoading = false; statusMessage = "✅ Connected!" }
                            .addOnFailureListener { isLoading = false; statusMessage = "❌ Connection error" }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TEST SYSTEM STATUS", color = Color.White.copy(0.4f), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun UserTypeRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(if (isSelected) AccentPurple else GlassWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.White.copy(0.4f), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = if (isSelected) Color.White else Color.White.copy(0.4f), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun GenderOption(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = if (isSelected) AccentPurple.copy(0.15f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(50.dp).border(1.dp, if (isSelected) AccentPurple else GlassBorder, RoundedCornerShape(14.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.White else Color.White.copy(0.4f), fontWeight = FontWeight.Bold)
        }
    }
}