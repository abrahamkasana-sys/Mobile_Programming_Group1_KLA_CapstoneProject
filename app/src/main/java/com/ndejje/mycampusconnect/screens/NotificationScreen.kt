package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "announcement", // announcement, event, club
    val relatedId: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filter by remember { mutableStateOf("all") } // all, unread

    val scope = rememberCoroutineScope()

    fun loadNotifications() {
        scope.launch {
            isLoading = true
            try {
                // For demo, create sample notifications
                notifications = listOf(
                    Notification(
                        notificationId = "1",
                        title = "New Announcement",
                        message = "Welcome to CampusConnect! Check out upcoming events.",
                        type = "announcement",
                        read = false,
                        createdAt = System.currentTimeMillis()
                    ),
                    Notification(
                        notificationId = "2",
                        title = "Event Reminder",
                        message = "Hackathon 2024 starts tomorrow!",
                        type = "event",
                        read = false,
                        createdAt = System.currentTimeMillis() - 86400000
                    ),
                    Notification(
                        notificationId = "3",
                        title = "Club Update",
                        message = "Computer Science Club meeting on Friday",
                        type = "club",
                        read = true,
                        createdAt = System.currentTimeMillis() - 172800000
                    )
                )
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    val filteredNotifications = if (filter == "unread") {
        notifications.filter { !it.read }
    } else {
        notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Filter button
                    IconButton(onClick = {
                        filter = if (filter == "all") "unread" else "all"
                    }) {
                        Badge(
                            containerColor = if (filter == "unread")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(if (filter == "unread") "Unread" else "All")
                        }
                    }

                    // Mark all as read
                    if (notifications.any { !it.read }) {
                        TextButton(
                            onClick = {
                                notifications = notifications.map { it.copy(read = true) }
                            }
                        ) {
                            Text("Mark all read")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            filteredNotifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,  // Changed from NotificationsNone to Notifications
                            contentDescription = "No notifications",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (filter == "unread") "No unread notifications" else "No notifications yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                // Mark as read
                                notifications = notifications.map {
                                    if (it.notificationId == notification.notificationId) {
                                        it.copy(read = true)
                                    } else it
                                }
                                // Navigate based on type
                                when (notification.type) {
                                    "event" -> navController.navigate("event_detail/${notification.relatedId}")
                                    "club" -> navController.navigate("club_detail/${notification.relatedId}")
                                    else -> { /* Just dismiss */ }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (notification.read) 1.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification icon
            Surface(
                shape = CircleShape,
                color = when (notification.type) {
                    "event" -> MaterialTheme.colorScheme.primaryContainer
                    "club" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (notification.type) {
                            "event" -> Icons.Default.DateRange  // Changed from Event to DateRange
                            "club" -> Icons.Default.Person  // Changed from People to Person
                            else -> Icons.Default.Info  // Changed from Announcement to Info
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            "event" -> MaterialTheme.colorScheme.primary
                            "club" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        .format(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Unread indicator
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}