package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedDataScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    var isAdding by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun addSampleEvents() {
        scope.launch {
            isAdding = true
            statusMessage = "Adding events..."
            try {
                val currentTime = System.currentTimeMillis()

                val events = listOf(
                    mapOf(
                        "eventId" to "event1",
                        "title" to "Welcome Week Festival",
                        "description" to "Join us for the annual Welcome Week festival with music, food, and games!",
                        "date" to currentTime + 7 * 24 * 60 * 60 * 1000,
                        "location" to "Main Campus Grounds",
                        "clubId" to "",
                        "imageUrl" to ""
                    ),
                    mapOf(
                        "eventId" to "event2",
                        "title" to "Tech Career Fair",
                        "description" to "Meet top employers and find internship opportunities in tech.",
                        "date" to currentTime + 14 * 24 * 60 * 60 * 1000,
                        "location" to "Business School Hall",
                        "clubId" to "",
                        "imageUrl" to ""
                    ),
                    mapOf(
                        "eventId" to "event3",
                        "title" to "Sports Tournament",
                        "description" to "Inter-faculty sports competition. Register your team now!",
                        "date" to currentTime + 21 * 24 * 60 * 60 * 1000,
                        "location" to "University Stadium",
                        "clubId" to "",
                        "imageUrl" to ""
                    ),
                    mapOf(
                        "eventId" to "event4",
                        "title" to "Cultural Night",
                        "description" to "Experience diverse cultures through music, dance, and food from around the world.",
                        "date" to currentTime + 10 * 24 * 60 * 60 * 1000,
                        "location" to "Multipurpose Hall",
                        "clubId" to "",
                        "imageUrl" to ""
                    )
                )

                for (event in events) {
                    firestore.collection("events").document(event["eventId"] as String).set(event).await()
                }

                statusMessage = "Success! Added ${events.size} events."
                Toast.makeText(context, "Sample events added successfully!", Toast.LENGTH_LONG).show()

                // Navigate back to home after 2 seconds
                kotlinx.coroutines.delay(2000)
                navController.navigate("home") {
                    popUpTo("seed_data") { inclusive = true }
                }

            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isAdding = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seed Database") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅 Add Sample Events",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This will add 4 sample events to your Firestore database:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("• 🎉 Welcome Week Festival")
                        Text("• 💼 Tech Career Fair")
                        Text("• ⚽ Sports Tournament")
                        Text("• 🌍 Cultural Night")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your home screen will show these events after adding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { addSampleEvents() },
                        enabled = !isAdding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adding...")
                        } else {
                            Text("Add Sample Events")
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (statusMessage!!.startsWith("Success"))
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}