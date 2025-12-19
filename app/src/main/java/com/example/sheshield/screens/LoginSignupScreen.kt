package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(
    onLoginSuccess: () -> Unit,
    onSwitchToHelperMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("user") } // user, helper, user_helper
    var gender by remember { mutableStateOf("") } // female, male
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Check if user is male - if yes, force helper mode
    val isMale = gender.lowercase() == "male"
    LaunchedEffect(gender) {
        if (isMale && !isLoginMode) {
            userType = "helper"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background decoration
        Surface(
            color = Color(0xFF6200EE),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "App Icon",
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF6200EE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SheShield",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isLoginMode) "Welcome Back" else "Create Account",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }

        // Form content - made scrollable
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Added scroll
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status message
                if (statusMessage.isNotEmpty()) {
                    Surface(
                        color = if (statusMessage.startsWith("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage,
                            color = if (statusMessage.startsWith("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF6200EE))
                    },
                    shape = RoundedCornerShape(12.dp)
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
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFF6200EE))
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Gender and User Type selection (only for signup)
                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender Selection
                    Text(
                        text = "Gender",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Only 2 gender options: Female and Male
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenderOption(
                            label = "Female",
                            isSelected = gender == "female",
                            onClick = {
                                gender = "female"
                                // Female can be any type, default to user
                                if (userType == "helper" && gender == "female") {
                                    userType = "user"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GenderOption(
                            label = "Male",
                            isSelected = gender == "male",
                            onClick = {
                                gender = "male"
                                // Male can only be helper
                                userType = "helper"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Type Selection (only if female)
                    if (!isMale && gender.isNotEmpty()) {
                        Text(
                            text = "Select User Type",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 8.dp)
                        )

                        // Vertical arrangement with dividers
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column {
                                // User (Need Help)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { userType = "user" }
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    if (userType == "user") Color(0xFF6200EE) else Color.LightGray.copy(alpha = 0.3f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "User",
                                                tint = if (userType == "user") Color.White else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "User (Need Help)",
                                                color = if (userType == "user") Color(0xFF6200EE) else Color.Black,
                                                fontWeight = if (userType == "user") FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                // Divider
                                Divider(color = Color.LightGray, thickness = 0.5.dp)

                                // Helper Only
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { userType = "helper" }
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    if (userType == "helper") Color(0xFF6200EE) else Color.LightGray.copy(alpha = 0.3f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SupervisorAccount,
                                                contentDescription = "Helper",
                                                tint = if (userType == "helper") Color.White else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Helper Only",
                                                color = if (userType == "helper") Color(0xFF6200EE) else Color.Black,
                                                fontWeight = if (userType == "helper") FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                // Divider
                                Divider(color = Color.LightGray, thickness = 0.5.dp)

                                // Both
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { userType = "user_helper" }
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    if (userType == "user_helper") Color(0xFF6200EE) else Color.LightGray.copy(alpha = 0.3f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SwitchAccount,
                                                contentDescription = "Both",
                                                tint = if (userType == "user_helper") Color.White else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Both",
                                                color = if (userType == "user_helper") Color(0xFF6200EE) else Color.Black,
                                                fontWeight = if (userType == "user_helper") FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isMale && gender.isNotEmpty()) {
                        // Show message for male users
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "As a male user, you can only register as a Helper",
                                    color = Color(0xFF1976D2),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Loading indicator
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Main action button
                Button(
                    onClick = {
                        // Validation
                        if (email.isEmpty() || password.isEmpty()) {
                            statusMessage = "❌ Please fill all fields"
                            return@Button
                        }

                        if (!isLoginMode && gender.isEmpty()) {
                            statusMessage = "❌ Please select your gender"
                            return@Button
                        }

                        if (!isLoginMode && !isMale && userType.isEmpty()) {
                            statusMessage = "❌ Please select user type"
                            return@Button
                        }

                        isLoading = true

                        if (isLoginMode) {
                            // LOGIN
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        statusMessage = "✅ Login successful!"
                                        onLoginSuccess()
                                    } else {
                                        statusMessage = "❌ Login failed: ${task.exception?.message ?: "Unknown error"}"
                                    }
                                }
                        } else {
                            // SIGN UP
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        // Create complete user document in Firestore
                                        val userData = hashMapOf(
                                            "email" to email,
                                            "userType" to userType,
                                            "gender" to gender,
                                            "name" to email.substringBefore("@"),
                                            "phone" to "",  // Empty initially
                                            "address" to "", // Empty initially
                                            "isHelperVerified" to false,
                                            "helperLevel" to 0,
                                            "createdAt" to System.currentTimeMillis(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )

                                        db.collection("users").document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                statusMessage = "✅ Account created! Please login"
                                                isLoginMode = true
                                                password = ""
                                                gender = ""
                                                userType = "user"
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                statusMessage = "❌ Failed to save user data: ${e.message}"
                                            }
                                    } else {
                                        isLoading = false
                                        statusMessage = "❌ Signup failed: ${task.exception?.message ?: "Unknown error"}"
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        if (isLoginMode) "LOGIN" else "SIGN UP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle between login/signup
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        statusMessage = ""
                        password = ""
                        gender = ""
                        userType = "user"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isLoginMode) "Don't have an account? Sign up" else "Already have an account? Login",
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Test Firebase connection button
                Button(
                    onClick = {
                        isLoading = true
                        val testData = hashMapOf(
                            "test" to "Firestore test",
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("test").document("compose_test")
                            .set(testData)
                            .addOnSuccessListener {
                                isLoading = false
                                statusMessage = "✅ Firestore connection successful!"
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                statusMessage = "❌ Firestore error: ${e.message}"
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Text("Test Firebase Connection")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun GenderOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Surface(
            color = if (isSelected) Color(0xFF6200EE).copy(alpha = 0.1f) else Color.Transparent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF6200EE) else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF6200EE) else Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}