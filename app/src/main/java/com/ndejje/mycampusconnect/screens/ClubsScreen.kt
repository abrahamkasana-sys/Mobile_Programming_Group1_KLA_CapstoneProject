package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(navController: NavController) {
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf("student") }
    var userClubId by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                if (currentUserId != null) {
                    val userDoc = firestore.collection("users").document(currentUserId).get().await()
                    userRole = userDoc.getString("role") ?: "student"
                    userClubId = userDoc.getString("clubId")
                }

                val snapshot = firestore.collection("clubs")
                    .orderBy("name")
                    .get()
                    .await()
                clubs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Club::class.java)?.copy(clubId = doc.id)
                }
                isLoading = false
            } catch (_: Exception) {
                errorMessage = "Failed to load clubs"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Clubs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (userRole == "admin") {
                        IconButton(onClick = { /* Navigate to create club screen */ }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Club")
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
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { /* Retry */ }) {
                            Text("Retry")
                        }
                    }
                }
            }
            clubs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No clubs found", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userRole == "admin") {
                            Button(onClick = { /* Navigate to create club */ }) {
                                Text("Create First Club")
                            }
                        }
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
                    items(clubs) { club ->
                        ClubCard(
                            club = club,
                            isJoined = userClubId == club.clubId,
                            userRole = userRole,
                            navController = navController,
                            onJoinClick = {
                                scope.launch {
                                    joinClub(currentUserId, club.clubId, firestore)
                                    userClubId = club.clubId
                                }
                            },
                            onLeaveClick = {
                                scope.launch {
                                    leaveClub(currentUserId, firestore)
                                    userClubId = null
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
fun ClubCard(
    club: Club,
    isJoined: Boolean,
    userRole: String,
    navController: NavController,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("club_detail/${club.clubId}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = club.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                when {
                    userRole == "admin" -> {
                        Button(
                            onClick = { /* Navigate to edit club */ },
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Edit")
                        }
                    }
                    isJoined -> {
                        OutlinedButton(
                            onClick = onLeaveClick,
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Leave")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onJoinClick,
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Join")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = club.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,  // Changed from Group to Person
                        contentDescription = "Members",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${club.memberCount} members",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isJoined) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Member",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(clubId: String, navController: NavController) {
    var club by remember { mutableStateOf<Club?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isJoined by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("student") }
    var clubEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(clubId) {
        scope.launch {
            try {
                val clubDoc = firestore.collection("clubs").document(clubId).get().await()
                club = clubDoc.toObject(Club::class.java)?.copy(clubId = clubDoc.id)

                if (currentUserId != null) {
                    val userDoc = firestore.collection("users").document(currentUserId).get().await()
                    userRole = userDoc.getString("role") ?: "student"
                    isJoined = userDoc.getString("clubId") == clubId
                }

                val eventsSnapshot = firestore.collection("events")
                    .whereEqualTo("clubId", clubId)
                    .orderBy("date")
                    .get()
                    .await()
                clubEvents = eventsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(eventId = doc.id)
                }

                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(club?.name ?: "Club Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (userRole == "admin" || (userRole == "club_leader" && isJoined)) {
                        IconButton(onClick = { /* Navigate to edit club */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Club")
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
            club == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Club not found")
                }
            }
            else -> {
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
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = club!!.name,
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Text(
                                    text = club!!.category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = club!!.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,  // Changed from Group to Person
                                        contentDescription = null
                                    )
                                    Text("${club!!.memberCount} Members")
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                when {
                                    userRole == "admin" -> {
                                        Button(
                                            onClick = { /* Navigate to manage club */ },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Manage Club (Admin)")
                                        }
                                    }
                                    isJoined -> {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    leaveClub(currentUserId, firestore)
                                                    isJoined = false
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Leave Club")
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    joinClub(currentUserId, clubId, firestore)
                                                    isJoined = true
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Join Club")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (clubEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = "Upcoming Events",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        items(clubEvents) { event ->
                            EventItemCard(event = event, navController = navController)
                        }
                    }

                    if ((userRole == "club_leader" && isJoined) || userRole == "admin") {
                        item {
                            Button(
                                onClick = { /* Navigate to create event */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create New Event")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
suspend fun joinClub(userId: String?, clubId: String, firestore: FirebaseFirestore) {
    if (userId == null) return

    firestore.collection("users").document(userId).update("clubId", clubId).await()

    val clubRef = firestore.collection("clubs").document(clubId)
    val club = clubRef.get().await()
    val currentCount = club.getLong("memberCount") ?: 0
    clubRef.update("memberCount", currentCount + 1).await()
}

suspend fun leaveClub(userId: String?, firestore: FirebaseFirestore) {
    if (userId == null) return

    val userDoc = firestore.collection("users").document(userId).get().await()
    val clubId = userDoc.getString("clubId")

    if (clubId != null) {
        firestore.collection("users").document(userId).update("clubId", null).await()

        val clubRef = firestore.collection("clubs").document(clubId)
        val club = clubRef.get().await()
        val currentCount = club.getLong("memberCount") ?: 0
        if (currentCount > 0) {
            clubRef.update("memberCount", currentCount - 1).await()
        }
    }
}

