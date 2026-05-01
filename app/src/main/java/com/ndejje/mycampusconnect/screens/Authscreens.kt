package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ndejje.mycampusconnect.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isLoginMode) {
        localError = null
        authViewModel.clearError()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon
            Card(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE94560).copy(alpha = 0.2f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isLoginMode) "🎓" else "📝", fontSize = 44.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLoginMode) "CampusConnect" else "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = if (isLoginMode) "Welcome back!" else "Join the campus community!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field (Register Mode Only)
            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; localError = null },
                    label = { Text("Full Name", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFE94560))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; localError = null },
                label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE94560))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE94560),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFFE94560),
                    cursorColor = Color(0xFFE94560),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; localError = null },
                label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE94560))
                },
                trailingIcon = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            if (passwordVisible) "Hide" else "Show",
                            color = Color(0xFFE94560),
                            fontSize = 12.sp
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE94560),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFFE94560),
                    cursorColor = Color(0xFFE94560),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (!isLoginMode) {
                        if (password != confirmPassword) {
                            localError = "Passwords don't match"
                            return@Button
                        }
                        if (password.length < 6) {
                            localError = "Password must be at least 6 characters"
                            return@Button
                        }
                        if (name.isBlank()) {
                            localError = "Please enter your name"
                            return@Button
                        }
                    }
                    if (email.isBlank()) {
                        localError = "Please enter your email"
                        return@Button
                    }
                    if (password.isBlank()) {
                        localError = "Please enter your password"
                        return@Button
                    }
                    localError = null

                    if (isLoginMode) {
                        authViewModel.login(email, password) {
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            onAuthSuccess()
                        }
                    } else {
                        authViewModel.register(email, password, name) {
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            onAuthSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (isLoginMode) "Login" else "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle between Login and Register
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Text(
                    text = if (isLoginMode) "Register" else "Login",
                    color = Color(0xFFE94560),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        isLoginMode = !isLoginMode
                        name = ""
                        confirmPassword = ""
                        password = ""
                        email = ""
                        passwordVisible = false
                        localError = null
                    }
                )
            }

            // Error Message
            localError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE94560).copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, color = Color(0xFFE94560), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}