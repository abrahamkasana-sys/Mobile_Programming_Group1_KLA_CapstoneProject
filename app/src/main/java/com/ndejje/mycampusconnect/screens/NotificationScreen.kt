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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "announcement",
    val relatedId: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filter by remember { mutableStateOf("all") }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun loadNotifications() {
        scope.launch {
            isLoading = true
            try {
                val userId = currentUser?.uid
                if (userId == null) {
                    isLoading = false
                    return@launch
                }

                // Query all notifications for this user
                val snapshot = firestore.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                notifications = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        NotificationItem(
                            notificationId = doc.id,
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            type = data["type"] as? String ?: "announcement",
                            relatedId = data["relatedId"] as? String ?: "",
                            read = data["read"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                            userId = data["userId"] as? String ?: ""
                        )
                    } else null
                }

                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    fun addSampleNotifications() {
        scope.launch {
            try {
                val userId = currentUser?.uid
                if (userId == null) {
                    Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val currentTime = System.currentTimeMillis()

                // Sample notifications
                val sampleNotifications = listOf(
                    mapOf(
                        "title" to "Welcome to CampusConnect!",
                        "message" to "Thank you for joining. Stay updated with campus events!",
                        "type" to "announcement",
                        "read" to false,
                        "createdAt" to currentTime,
                        "userId" to userId
                    ),
                    mapOf(
                        "title" to "Tech Career Fair",
                        "message" to "Don't miss the Tech Career Fair this Friday!",
                        "type" to "event",
                        "read" to false,
                        "createdAt" to currentTime - 86400000,
                        "userId" to userId
                    ),
                    mapOf(
                        "title" to "New Club Alert",
                        "message" to "The Photography Club has been formed. Join today!",
                        "type" to "club",
                        "read" to false,
                        "createdAt" to currentTime - 172800000,
                        "userId" to userId
                    ),
                    mapOf(
                        "title" to "Sports Tournament",
                        "message" to "Register for the inter-faculty sports tournament!",
                        "type" to "event",
                        "read" to false,
                        "createdAt" to currentTime - 259200000,
                        "userId" to userId
                    )
                )

                for (notification in sampleNotifications) {
                    firestore.collection("notifications").add(notification).await()
                }

                Toast.makeText(context, "4 Sample notifications added!", Toast.LENGTH_SHORT).show()

                // Reload notifications
                loadNotifications()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()

            notifications = notifications.map {
                if (it.notificationId == notificationId) it.copy(read = true) else it
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()

            notifications = notifications.filter { it.notificationId != notificationId }
            Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error deleting notification", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun markAllAsRead() {
        try {
            val unreadNotifications = notifications.filter { !it.read }

            for (notification in unreadNotifications) {
                firestore.collection("notifications")
                    .document(notification.notificationId)
                    .update("read", true)
                    .await()
            }

            notifications = notifications.map { it.copy(read = true) }
            Toast.makeText(context, "All notifications marked as read", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error marking as read", Toast.LENGTH_SHORT).show()
        }
    }

    // Load notifications on first load and when refreshTrigger changes
    LaunchedEffect(Unit, refreshTrigger) {
        loadNotifications()
    }

    val filteredNotifications = if (filter == "unread") {
        notifications.filter { !it.read }
    } else {
        notifications
    }

    val unreadCount = notifications.count { !it.read }

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
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                        text = "Notifications",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Row {
                        if (unreadCount > 0) {
                            TextButton(onClick = { scope.launch { markAllAsRead() } }) {
                                Text("Mark all read", color = Color(0xFFE94560), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Stats Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${notifications.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text("Total", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$unreadCount",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE94560)
                            )
                            Text("Unread", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filter == "all",
                        onClick = { filter = "all" },
                        label = {
                            Text(
                                "All",
                                fontSize = 13.sp,
                                color = if (filter == "all") Color(0xFFE94560) else Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.2f),
                            containerColor = Color.White.copy(alpha = 0.05f),
                            selectedLabelColor = Color(0xFFE94560),
                            labelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = filter == "unread",
                        onClick = { filter = "unread" },
                        label = {
                            Text(
                                "Unread",
                                fontSize = 13.sp,
                                color = if (filter == "unread") Color(0xFFE94560) else Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.2f),
                            containerColor = Color.White.copy(alpha = 0.05f),
                            selectedLabelColor = Color(0xFFE94560),
                            labelColor = Color.White
                        )
                    )
                }

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFE94560))
                        }
                    }
                    filteredNotifications.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔔", fontSize = 64.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (filter == "unread") "No unread notifications" else "No notifications yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Click the + button to add sample notifications",
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
                            items(filteredNotifications) { notification ->
                                ModernNotificationCard(
                                    notification = notification,
                                    onMarkRead = { scope.launch { markAsRead(notification.notificationId) } },
                                    onDelete = { scope.launch { deleteNotification(notification.notificationId) } },
                                    onClick = {
                                        if (!notification.read) {
                                            scope.launch { markAsRead(notification.notificationId) }
                                        }
                                    }
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
fun ModernNotificationCard(
    notification: NotificationItem,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val (icon, color) = when (notification.type) {
        "event" -> Pair("📅", Color(0xFF2196F3))
        "club" -> Pair("👥", Color(0xFF4CAF50))
        else -> Pair("📢", Color(0xFFE94560))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                Color.White.copy(alpha = 0.05f)
            else
                Color.White.copy(alpha = 0.12f)
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(notification.createdAt),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                // Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF16213E))
                    ) {
                        if (!notification.read) {
                            DropdownMenuItem(
                                text = { Text("Mark as read", color = Color.White) },
                                onClick = {
                                    onMarkRead()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFE94560)) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE94560), modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            // Unread indicator dot
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE94560), CircleShape)
                    )
                }
            }
        }
    }
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