package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import com.ndejje.mycampusconnect.models.Club
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("student") }
    var userClubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun loadUserData() {
        try {
            val userId = currentUser?.uid ?: return

            val userDoc = firestore.collection("users").document(userId).get().await()
            userName = userDoc.getString("name") ?: currentUser.displayName ?: "User"
            userEmail = currentUser.email ?: ""
            userRole = userDoc.getString("role") ?: "student"

            @Suppress("UNCHECKED_CAST")
            val userClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()

            if (userClubIds.isNotEmpty()) {
                val clubsList = mutableListOf<Club>()
                for (clubId in userClubIds) {
                    val clubDoc = firestore.collection("clubs").document(clubId).get().await()
                    val club = clubDoc.toObject(Club::class.java)?.copy(clubId = clubDoc.id)
                    club?.let { clubsList.add(it) }
                }
                userClubs = clubsList
            }

            isLoading = false
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUserData()
    }

    fun getCategoryDisplayName(category: String): String {
        return when (category) {
            "COMMUNITY_SERVICE" -> "Community Service"
            "LEADERSHIP" -> "Leadership"
            "CULTURAL" -> "Cultural"
            "RELIGIOUS" -> "Religious"
            "PROFESSIONAL" -> "Professional"
            "SPORTS" -> "Sports"
            "SPECIAL_INTEREST" -> "Special Interest"
            else -> category
        }
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
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { navController.navigate("edit_profile") },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Profile Header
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFFE94560), Color(0xFFF5A623))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = userName.take(2).uppercase(),
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = userName,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Text(
                                            text = userEmail,
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (userRole == "admin")
                                                Color(0xFFE94560).copy(alpha = 0.2f)
                                            else
                                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (userRole == "admin") "👑 Administrator" else "🎓 Student",
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (userRole == "admin") Color(0xFFE94560) else Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                }
                            }

                            // Stats Row
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StatsCard(
                                        title = "Clubs",
                                        value = "${userClubs.size}",
                                        icon = "🎯",
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatsCard(
                                        title = "Events",
                                        value = "0",
                                        icon = "📅",
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatsCard(
                                        title = "Points",
                                        value = "0",
                                        icon = "⭐",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // My Clubs Section
                            if (userClubs.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "My Clubs",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                items(userClubs) { club ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("club_detail/${club.clubId}")
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(Color(0xFFE94560), Color(0xFFF5A623))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = club.name.take(2).uppercase(),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = club.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = getCategoryDisplayName(club.category),
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFE94560)
                                                )
                                            }

                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "View",
                                                tint = Color.White.copy(alpha = 0.5f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                item {
                                    TextButton(
                                        onClick = { navController.navigate("clubs") },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Browse More Clubs", color = Color(0xFFE94560))
                                    }
                                }
                            } else {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.05f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🎯", fontSize = 48.sp)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "No Clubs Yet",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                "Join clubs to see them here",
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { navController.navigate("clubs") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFE94560)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Explore Clubs", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            // Logout Button (no icon)
                            item {
                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE94560).copy(alpha = 0.15f),
                                        contentColor = Color(0xFFE94560)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}