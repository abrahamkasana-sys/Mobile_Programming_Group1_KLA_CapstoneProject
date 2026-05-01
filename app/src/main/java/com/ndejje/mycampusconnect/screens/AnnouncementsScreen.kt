package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isImportant: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(navController: NavController) {
    val context = LocalContext.current
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun loadAnnouncements() {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("announcements")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()

                announcements = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        Announcement(
                            announcementId = doc.id,
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                            isImportant = data["isImportant"] as? Boolean ?: false
                        )
                    } else null
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAnnouncements()
    }

    fun formatDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 604800000 -> "${diff / 86400000} days ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
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
                // Custom Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Text(
                        text = "Announcements",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Box(modifier = Modifier.size(48.dp))
                }

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFE94560))
                        }
                    }
                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Error: $errorMessage", color = Color.White.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { loadAnnouncements() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    announcements.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📢", fontSize = 64.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Announcements",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Check back later for updates",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(announcements) { announcement ->
                                AnnouncementCard(
                                    announcement = announcement,
                                    formatDate = { formatDate(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    formatDate: (Long) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (announcement.isImportant)
                Color(0xFFE94560).copy(alpha = 0.15f)
            else
                Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (announcement.isImportant)
                            Color(0xFFE94560).copy(alpha = 0.3f)
                        else
                            Color(0xFF2196F3).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (announcement.isImportant) "🔴" else "📢",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = announcement.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    if (announcement.isImportant) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE94560)
                        ) {
                            Text(
                                text = "IMPORTANT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = announcement.message,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatDate(announcement.createdAt),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

suspend fun addSampleAnnouncements() {
    val firestore = FirebaseFirestore.getInstance()
    val announcements = listOf(
        mapOf(
            "title" to "Welcome Week Celebration!",
            "message" to "Join us for the welcome week activities starting next Monday. Meet new friends and explore campus!",
            "createdAt" to System.currentTimeMillis(),
            "isImportant" to true,
            "imageUrl" to ""
        ),
        mapOf(
            "title" to "Library Hours Extended",
            "message" to "The university library will be open until midnight during exam week.",
            "createdAt" to System.currentTimeMillis() - 86400000,
            "isImportant" to false,
            "imageUrl" to ""
        ),
        mapOf(
            "title" to "New Club: Photography Club",
            "message" to "The Photography Club has been officially formed. Join to learn photography skills!",
            "createdAt" to System.currentTimeMillis() - 172800000,
            "isImportant" to false,
            "imageUrl" to ""
        )
    )

    for (announcement in announcements) {
        firestore.collection("announcements").add(announcement).await()
    }
}