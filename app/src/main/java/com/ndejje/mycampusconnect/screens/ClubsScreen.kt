package com.ndejje.mycampusconnect.screens

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Import EventItemCard from EventsScreen - NO DUPLICATE!
// Make sure EventItemCard is defined in EventsScreen.kt

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
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf("student") }
    var userClubId by remember { mutableStateOf<String?>(null) }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Pull to refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Load data function
    suspend fun loadData() {
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
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load clubs"
        }
    }

    // Load data when screen opens
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            loadData()
            isLoading = false
        }
    }

    // Filter clubs based on search query and selected category
    val filteredClubs = remember(clubs, searchQuery, selectedCategory) {
        clubs.filter { club ->
            // Category filter
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                else -> {
                    val dbCategory = mapToDatabaseCategory(selectedCategory)
                    club.category == dbCategory
                }
            }

            // Search filter
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
                    IconButton(onClick = {
                        navController.navigate("setup_data")
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Setup Database")
                    }
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
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                loadData()
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search clubs by name or description...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

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
                                        if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                                            Text(
                                                text = "Try adjusting your search or filter",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredClubs) { club ->
                                        ClubCard(
                                            club = club,
                                            isJoined = userClubId == club.clubId,
                                            userRole = userRole,
                                            navController = navController,
                                            onJoinClick = {
                                                scope.launch {
                                                    joinClub(currentUserId, club.clubId, firestore)
                                                    userClubId = club.clubId
                                                    // Update member count in local list
                                                    clubs = clubs.map {
                                                        if (it.clubId == club.clubId) {
                                                            it.copy(memberCount = it.memberCount + 1)
                                                        } else it
                                                    }
                                                }
                                            },
                                            onLeaveClick = {
                                                scope.launch {
                                                    leaveClub(currentUserId, firestore)
                                                    userClubId = null
                                                    // Update member count in local list
                                                    clubs = clubs.map {
                                                        if (it.clubId == club.clubId) {
                                                            it.copy(memberCount = it.memberCount - 1)
                                                        } else it
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
    // Get display category name
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

                when {
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
                        IconButton(onClick = {
                            navController.navigate("edit_club/$clubId")
                        }) {
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

                                val displayCategory = when (club!!.category) {
                                    "COMMUNITY_SERVICE" -> "Community Service"
                                    "LEADERSHIP" -> "Leadership"
                                    "CULTURAL" -> "Cultural"
                                    "RELIGIOUS" -> "Religious"
                                    "PROFESSIONAL" -> "Professional"
                                    "SPORTS" -> "Sports"
                                    "SPECIAL_INTEREST" -> "Special Interest"
                                    else -> club!!.category
                                }

                                Text(
                                    text = displayCategory,
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
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null
                                    )
                                    Text("${club!!.memberCount} Members")
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                when {
                                    userRole == "admin" -> {
                                        Button(
                                            onClick = {
                                                navController.navigate("manage_club/$clubId")
                                            },
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
                                                    val updatedClub = firestore.collection("clubs")
                                                        .document(clubId).get().await()
                                                    club = updatedClub.toObject(Club::class.java)
                                                        ?.copy(clubId = clubId)
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
                                                    val updatedClub = firestore.collection("clubs")
                                                        .document(clubId).get().await()
                                                    club = updatedClub.toObject(Club::class.java)
                                                        ?.copy(clubId = clubId)
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
                            // Using EventItemCard from EventsScreen.kt - NO duplicate!
                            EventItemCard(event = event, navController = navController)
                        }
                    }

                    if ((userRole == "club_leader" && isJoined) || userRole == "admin") {
                        item {
                            Button(
                                onClick = {
                                    navController.navigate("create_event?clubId=$clubId")
                                },
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