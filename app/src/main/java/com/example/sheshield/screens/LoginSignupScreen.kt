package com.example.sheshield.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("regular") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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

        // Form content
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
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp),
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

                // User type selection (only for signup)
                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select User Type",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            RadioButton(
                                selected = userType == "regular",
                                onClick = { userType = "regular" },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6200EE)
                                )
                            )
                            Text("User (Need Help)", fontWeight = FontWeight.Medium)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            RadioButton(
                                selected = userType == "helper",
                                onClick = { userType = "helper" },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6200EE)
                                )
                            )
                            Text("Helper", fontWeight = FontWeight.Medium)
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
                        if (email.isEmpty() || password.isEmpty()) {
                            statusMessage = "❌ Please fill all fields"
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
                                            "name" to email.substringBefore("@"),
                                            "phone" to "",  // Empty initially
                                            "address" to "", // Empty initially
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
            }
        }
    }
}