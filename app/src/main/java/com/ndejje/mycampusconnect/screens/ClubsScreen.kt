package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Category list for filtering
val clubCategories = listOf(
    "All",
    "Community Service",
    "Leadership",
    "Cultural",
    "Religious",
    "Professional",
    "Sports",
    "Special Interest"
)

// Map display category to database category
fun mapToDatabaseCategory(displayCategory: String): String? {
    return when (displayCategory) {
        "Community Service" -> "COMMUNITY_SERVICE"
        "Leadership" -> "LEADERSHIP"
        "Cultural" -> "CULTURAL"
        "Religious" -> "RELIGIOUS"
        "Professional" -> "PROFESSIONAL"
        "Sports" -> "SPORTS"
        "Special Interest" -> "SPECIAL_INTEREST"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(navController: NavController) {
    val context = LocalContext.current
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf("student") }
    var userClubIds by remember { mutableStateOf<List<String>>(emptyList()) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    suspend fun loadData() {
        try {
            if (currentUserId != null) {
                val userDoc = firestore.collection("users").document(currentUserId).get().await()
                userRole = userDoc.getString("role") ?: "student"
                @Suppress("UNCHECKED_CAST")
                userClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()
                android.util.Log.d("ClubsScreen", "User club IDs: $userClubIds")
            }

            val snapshot = firestore.collection("clubs")
                .orderBy("name")
                .get()
                .await()
            clubs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Club::class.java)?.copy(clubId = doc.id)
            }
            android.util.Log.d("ClubsScreen", "Loaded ${clubs.size} clubs")
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load clubs"
            android.util.Log.e("ClubsScreen", "Error loading clubs", e)
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            loadData()
            isLoading = false
        }
    }

    val filteredClubs = remember(clubs, searchQuery, selectedCategory) {
        clubs.filter { club ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                else -> {
                    val dbCategory = mapToDatabaseCategory(selectedCategory)
                    club.category == dbCategory
                }
            }

            val matchesSearch = searchQuery.isEmpty() ||
                    club.name.contains(searchQuery, ignoreCase = true) ||
                    club.description.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
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
                        IconButton(onClick = {
                            navController.navigate("create_club")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Club")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    loadData()
                                    isLoading = false
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Category Chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(clubCategories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    modifier = Modifier.clip(RoundedCornerShape(32.dp))
                                )
                            }
                        }

                        // Clubs List
                        if (filteredClubs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "No results",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No clubs found",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredClubs) { club ->
                                    var isJoining by remember { mutableStateOf(false) }

                                    ClubCard(
                                        club = club,
                                        isJoined = userClubIds.contains(club.clubId),
                                        userRole = userRole,
                                        navController = navController,
                                        isJoining = isJoining,
                                        onJoinClick = {
                                            if (userClubIds.contains(club.clubId)) {
                                                Toast.makeText(context, "Already a member!", Toast.LENGTH_SHORT).show()
                                                return@ClubCard
                                            }
                                            isJoining = true
                                            scope.launch {
                                                try {
                                                    android.util.Log.d("ClubsScreen", "Joining club: ${club.clubId}")
                                                    val success = joinClub(currentUserId, club.clubId, firestore)
                                                    if (success) {
                                                        userClubIds = userClubIds + club.clubId
                                                        clubs = clubs.map {
                                                            if (it.clubId == club.clubId) {
                                                                it.copy(memberCount = it.memberCount + 1)
                                                            } else it
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "Joined ${club.name}!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        android.util.Log.d("ClubsScreen", "Successfully joined")
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to join club. You might already be a member.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        android.util.Log.e("ClubsScreen", "Join failed for club: ${club.clubId}")
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("ClubsScreen", "Error joining club", e)
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } finally {
                                                    isJoining = false
                                                }
                                            }
                                        },
                                        onLeaveClick = {
                                            isJoining = true
                                            scope.launch {
                                                try {
                                                    android.util.Log.d("ClubsScreen", "Leaving club: ${club.clubId}")
                                                    val success = leaveClub(currentUserId, club.clubId, firestore)
                                                    if (success) {
                                                        userClubIds = userClubIds.filter { it != club.clubId }
                                                        clubs = clubs.map {
                                                            if (it.clubId == club.clubId) {
                                                                it.copy(memberCount = it.memberCount - 1)
                                                            } else it
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "Left ${club.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to leave club",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } finally {
                                                    isJoining = false
                                                }
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
}

@Composable
fun ClubCard(
    club: Club,
    isJoined: Boolean,
    userRole: String,
    navController: NavController,
    isJoining: Boolean,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    val displayCategory = when (club.category) {
        "COMMUNITY_SERVICE" -> "Community Service"
        "LEADERSHIP" -> "Leadership"
        "CULTURAL" -> "Cultural"
        "RELIGIOUS" -> "Religious"
        "PROFESSIONAL" -> "Professional"
        "SPORTS" -> "Sports"
        "SPECIAL_INTEREST" -> "Special Interest"
        else -> club.category
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Only navigate to detail when clicking the card, not the button
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
                        text = displayCategory,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Button area - click doesn't propagate to card
                Row {
                    when {
                        isJoining -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        userRole == "admin" -> {
                            Button(
                                onClick = {
                                    navController.navigate("edit_club/${club.clubId}")
                                },
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
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

                if (isJoined && !isJoining) {
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

// Helper functions
suspend fun joinClub(userId: String?, clubId: String, firestore: FirebaseFirestore): Boolean {
    if (userId == null) {
        android.util.Log.e("JoinClub", "User ID is null")
        return false
    }

    return try {
        android.util.Log.d("JoinClub", "Starting join process for user: $userId, club: $clubId")

        // Get current user data
        val userDoc = firestore.collection("users").document(userId).get().await()
        @Suppress("UNCHECKED_CAST")
        val currentClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()

        android.util.Log.d("JoinClub", "Current club IDs: $currentClubIds")

        // Check if already a member
        if (currentClubIds.contains(clubId)) {
            android.util.Log.d("JoinClub", "User already a member")
            return false
        }

        // Add new club ID to the list
        val updatedClubIds = currentClubIds + clubId
        android.util.Log.d("JoinClub", "Updated club IDs: $updatedClubIds")

        // Update user's clubIds array
        firestore.collection("users").document(userId).update("clubIds", updatedClubIds).await()

        // Increment club member count
        val clubRef = firestore.collection("clubs").document(clubId)
        val club = clubRef.get().await()
        val currentCount = club.getLong("memberCount") ?: 0
        clubRef.update("memberCount", currentCount + 1).await()

        android.util.Log.d("JoinClub", "Successfully joined club")
        true
    } catch (e: Exception) {
        android.util.Log.e("JoinClub", "Error joining club: ${e.message}", e)
        false
    }
}

suspend fun leaveClub(userId: String?, clubId: String, firestore: FirebaseFirestore): Boolean {
    if (userId == null) {
        android.util.Log.e("LeaveClub", "User ID is null")
        return false
    }

    return try {
        android.util.Log.d("LeaveClub", "Starting leave process")

        val userDoc = firestore.collection("users").document(userId).get().await()
        @Suppress("UNCHECKED_CAST")
        val currentClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()

        if (!currentClubIds.contains(clubId)) {
            android.util.Log.d("LeaveClub", "User is not a member")
            return false
        }

        val updatedClubIds = currentClubIds.filter { it != clubId }
        firestore.collection("users").document(userId).update("clubIds", updatedClubIds).await()

        val clubRef = firestore.collection("clubs").document(clubId)
        val club = clubRef.get().await()
        val currentCount = club.getLong("memberCount") ?: 0
        if (currentCount > 0) {
            clubRef.update("memberCount", currentCount - 1).await()
        }

        android.util.Log.d("LeaveClub", "Successfully left club")
        true
    } catch (e: Exception) {
        android.util.Log.e("LeaveClub", "Error leaving club: ${e.message}", e)
        false
    }
}

// ClubDetailScreen (simplified)
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
                    @Suppress("UNCHECKED_CAST")
                    val clubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()
                    isJoined = clubIds.contains(clubId)
                }

                val eventsSnapshot = firestore.collection("events")
                    .whereEqualTo("clubId", clubId)
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
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            club == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Club not found")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(club!!.name, style = MaterialTheme.typography.headlineSmall)
                                Text(club!!.description, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${club!!.memberCount} Members")
                            }
                        }
                    }
                }
            }
        }
    }
}