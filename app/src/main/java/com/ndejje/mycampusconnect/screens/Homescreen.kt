package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.reporting.MessagingClientEvent
import com.ndejje.mycampusconnect.models.Announcement
import com.ndejje.mycampusconnect.models.Event
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var recentEvents by remember { mutableStateOf<List<MessagingClientEvent.Event>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val eventsSnapshot = firestore.collection("events")
                    .orderBy("date")
                    .limit(3)
                    .get()
                    .await()
                recentEvents = eventsSnapshot.documents.mapNotNull { it.toObject(Event::class.java) }

                val announcementsSnapshot = firestore.collection("announcements")
                    .orderBy("createdAt")
                    .limit(5)
                    .get()
                    .await()
                announcements = announcementsSnapshot.documents.mapNotNull { it.toObject(Announcement::class.java) }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Welcome to CampusConnect!", style = MaterialTheme.typography.headlineSmall)
                        Text("Stay updated with campus events and announcements")
                    }
                }
            }

            item {
                Text("Upcoming Events", style = MaterialTheme.typography.titleLarge)
                if (recentEvents.isEmpty() && !isLoading) {
                    Text("No upcoming events")
                }
            }

            items(recentEvents) { event ->
                EventCard(event, navController)
            }

            item {
                Text("Recent Announcements", style = MaterialTheme.typography.titleLarge)
            }

            items(announcements) { announcement ->
                AnnouncementCard(announcement)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") },
            selected = false,
            onClick = { navController.navigate("events") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.People, contentDescription = "Clubs") },
            label = { Text("Clubs") },
            selected = false,
            onClick = { navController.navigate("clubs") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Lost & Found") },
            label = { Text("Lost") },
            selected = false,
            onClick = { navController.navigate("lost_and_found") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
fun EventCard(event: Event, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("event_detail/${event.eventId}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
            Text("📍 ${event.location}", style = MaterialTheme.typography.bodySmall)
            Text("📅 ${java.text.SimpleDateFormat("MMM dd, yyyy").format(event.date)}")
        }
    }
}

@Composable
fun AnnouncementCard(announcement: Announcement) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(announcement.title, style = MaterialTheme.typography.titleSmall)
            Text(announcement.content, style = MaterialTheme.typography.bodySmall)
            Text("— ${announcement.authorName}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// Add missing imports
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable