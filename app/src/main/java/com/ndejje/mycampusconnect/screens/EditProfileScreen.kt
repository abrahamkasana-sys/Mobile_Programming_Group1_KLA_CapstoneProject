package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load current user data
    suspend fun loadUserData() {
        try {
            val userId = currentUser?.uid ?: return
            val userDoc = firestore.collection("users").document(userId).get().await()
            userName = userDoc.getString("name") ?: currentUser.displayName ?: ""
            isLoading = false
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUserData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
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
                    .padding(paddingValues)
            ) {
                // Custom Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Edit Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    try {
                                        val userId = currentUser?.uid ?: return@launch
                                        firestore.collection("users").document(userId)
                                            .update("name", userName)
                                            .await()
                                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                        navController.navigateUp()
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && userName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE94560),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saving...", fontSize = 14.sp)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 14.sp)
                            }
                        }
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE94560))
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Avatar
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFE94560), Color(0xFFF5A623))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(2).uppercase().ifEmpty { "U" },
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Profile Information",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Update your personal details",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Display Name Field
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Display Name", color = Color.White.copy(alpha = 0.7f)) },
                                placeholder = { Text("Enter your name", color = Color.White.copy(alpha = 0.5f)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFE94560)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE94560),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFE94560),
                                    cursorColor = Color(0xFFE94560),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email Field (read-only)
                            OutlinedTextField(
                                value = currentUser?.email ?: "",
                                onValueChange = {},
                                label = { Text("Email Address", color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFFE94560)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE94560),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFE94560),
                                    cursorColor = Color(0xFFE94560),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    disabledBorderColor = Color.White.copy(alpha = 0.2f),
                                    disabledLabelColor = Color.White.copy(alpha = 0.4f),
                                    disabledTextColor = Color.White.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Info Card
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
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFE94560),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Email address cannot be changed. Contact support for assistance.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE94560)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Warning about changes
                            if (userName != currentUser?.displayName && userName.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Your display name will be updated across the app",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}