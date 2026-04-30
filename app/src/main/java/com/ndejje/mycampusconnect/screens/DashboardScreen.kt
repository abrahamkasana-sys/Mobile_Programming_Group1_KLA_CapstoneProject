package com.ndejje.mycampusconnect.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = currentUser?.uid
                if (userId != null) {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    userName = userDoc.getString("name") ?: currentUser.displayName ?: "Student"
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
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
            // Animated background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE94560).copy(alpha = 0.15f),
                                Color(0xFF533483).copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = Offset(
                                x = 200f + animatedOffset * 0.5f,
                                y = 300f + animatedOffset * 0.3f
                            ),
                            radius = 400f
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(greeting, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Welcome to CampusConnect!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        IconButton(
                            onClick = { navController.navigate("notifications") },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
                        }
                    }
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Events Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable { navController.navigate("events") },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFFE94560), Color(0xFFF5A623))),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(Icons.Default.DateRange, "Events", tint = Color.White, modifier = Modifier.size(28.dp))
                                    Column {
                                        Text("24", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Events", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }

                        // Clubs Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable { navController.navigate("clubs") },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF2196F3), Color(0xFF21CBF3))),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(Icons.Default.Person, "Clubs", tint = Color.White, modifier = Modifier.size(28.dp))
                                    Column {
                                        Text("20", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Clubs", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }

                        // Members Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(Icons.Default.Person, "Members", tint = Color.White, modifier = Modifier.size(28.dp))
                                    Column {
                                        Text("1.2k", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Members", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Actions
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Lost Item
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .clickable { navController.navigate("post_lost_item") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE5A24))),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Warning, "Lost Item", tint = Color.White, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Lost Item", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            // Find Clubs
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .clickable { navController.navigate("clubs") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2))),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Search, "Find Clubs", tint = Color.White, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Find Clubs", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            // My Profile
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .clickable { navController.navigate("profile") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Person, "My Profile", tint = Color.White, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("My Profile", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Upcoming Events Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upcoming Events", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        TextButton(onClick = { navController.navigate("events") }) {
                            Text("See All", color = Color(0xFFE94560), fontSize = 12.sp)
                        }
                    }
                }

                // Event Card 1 (Sample)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { navController.navigate("events") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE94560), modifier = Modifier.size(60.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("DEC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("15", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome Week Festival", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.6f))
                                    Text("Main Campus Grounds", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "View", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Event Card 2 (Sample)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { navController.navigate("events") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE94560), modifier = Modifier.size(60.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("DEC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("22", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tech Career Fair", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.6f))
                                    Text("Business School Hall", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "View", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Recent Announcements Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recent Announcements", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }

                // Announcement 1
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE94560).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome Week Celebration!", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("Join us for welcome week activities starting next Monday!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 2)
                                Text("2 hours ago", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                // Announcement 2
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE94560).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Library Hours Extended", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("The university library will be open until midnight during exam week.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 2)
                                Text("Yesterday", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                // Popular Clubs Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Popular Clubs", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        TextButton(onClick = { navController.navigate("clubs") }) {
                            Text("See All", color = Color(0xFFE94560), fontSize = 12.sp)
                        }
                    }
                }

                // Clubs Row
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val clubsList = listOf(
                            "Rotaract Club" to "🤝",
                            "Student Guild" to "👥",
                            "Football Club" to "⚽",
                            "Tech Club" to "💻"
                        )
                        items(clubsList) { (name, emoji) ->
                            Card(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { navController.navigate("clubs") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(emoji, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}