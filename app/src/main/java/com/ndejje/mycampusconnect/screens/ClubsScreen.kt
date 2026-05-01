package com.ndejje.mycampusconnect.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Campus Clubs",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                }

                // Search Bar
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
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Simple TextField instead of BasicTextField
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search clubs...", color = Color.White.copy(alpha = 0.4f)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
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
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Error: $errorMessage", color = Color.White.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            loadData()
                                            isLoading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
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
                                        label = {
                                            Text(
                                                category,
                                                fontSize = 13.sp,
                                                color = if (selectedCategory == category) Color(0xFFE94560) else Color.White
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
                            }

                            // Clubs List
                            if (filteredClubs.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🎯", fontSize = 64.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No clubs found",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "Try adjusting your search or filter",
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.5f)
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

                                        ModernClubCard(
                                            club = club,
                                            isJoined = userClubIds.contains(club.clubId),
                                            userRole = userRole,
                                            navController = navController,
                                            isJoining = isJoining,
                                            onJoinClick = {
                                                if (userClubIds.contains(club.clubId)) {
                                                    Toast.makeText(context, "Already a member!", Toast.LENGTH_SHORT).show()
                                                    return@ModernClubCard
                                                }
                                                isJoining = true
                                                scope.launch {
                                                    try {
                                                        val success = joinClub(currentUserId, club.clubId, firestore)
                                                        if (success) {
                                                            userClubIds = userClubIds + club.clubId
                                                            clubs = clubs.map {
                                                                if (it.clubId == club.clubId) {
                                                                    it.copy(memberCount = it.memberCount + 1)
                                                                } else it
                                                            }
                                                            Toast.makeText(context, "Joined ${club.name}!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to join club", Toast.LENGTH_LONG).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                    } finally {
                                                        isJoining = false
                                                    }
                                                }
                                            },
                                            onLeaveClick = {
                                                isJoining = true
                                                scope.launch {
                                                    try {
                                                        val success = leaveClub(currentUserId, club.clubId, firestore)
                                                        if (success) {
                                                            userClubIds = userClubIds.filter { it != club.clubId }
                                                            clubs = clubs.map {
                                                                if (it.clubId == club.clubId) {
                                                                    it.copy(memberCount = it.memberCount - 1)
                                                                } else it
                                                            }
                                                            Toast.makeText(context, "Left ${club.name}", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to leave club", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
}

@Composable
fun ModernClubCard(
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

    val categoryIcon = when (club.category) {
        "COMMUNITY_SERVICE" -> "🤝"
        "LEADERSHIP" -> "👥"
        "CULTURAL" -> "🎭"
        "RELIGIOUS" -> "⛪"
        "PROFESSIONAL" -> "💼"
        "SPORTS" -> "⚽"
        "SPECIAL_INTEREST" -> "⭐"
        else -> "🏛️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("club_detail/${club.clubId}")
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Icon Circle
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
                        Text(categoryIcon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = club.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = displayCategory,
                            fontSize = 11.sp,
                            color = Color(0xFFE94560)
                        )
                    }
                }

                // Action Button
                when {
                    isJoining -> {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFFE94560), strokeWidth = 2.dp)
                    }
                    userRole == "admin" -> {
                        OutlinedButton(
                            onClick = { navController.navigate("edit_club/${club.clubId}") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE94560)
                            )
                        ) {
                            Text("Edit", fontSize = 12.sp)
                        }
                    }
                    isJoined -> {
                        OutlinedButton(
                            onClick = onLeaveClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE94560)
                            )
                        ) {
                            Text("Leave", fontSize = 12.sp)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onJoinClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE94560)
                            )
                        ) {
                            Text("Join", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = club.description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Members",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${club.memberCount} members",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                if (isJoined && !isJoining) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "✓ Member",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
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
        return false
    }

    return try {
        val userDoc = firestore.collection("users").document(userId).get().await()
        @Suppress("UNCHECKED_CAST")
        val currentClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()

        if (currentClubIds.contains(clubId)) {
            return false
        }

        val updatedClubIds = currentClubIds + clubId
        firestore.collection("users").document(userId).update("clubIds", updatedClubIds).await()

        val clubRef = firestore.collection("clubs").document(clubId)
        val club = clubRef.get().await()
        val currentCount = club.getLong("memberCount") ?: 0
        clubRef.update("memberCount", currentCount + 1).await()

        true
    } catch (e: Exception) {
        false
    }
}

suspend fun leaveClub(userId: String?, clubId: String, firestore: FirebaseFirestore): Boolean {
    if (userId == null) {
        return false
    }

    return try {
        val userDoc = firestore.collection("users").document(userId).get().await()
        @Suppress("UNCHECKED_CAST")
        val currentClubIds = (userDoc.get("clubIds") as? List<String>) ?: emptyList()

        if (!currentClubIds.contains(clubId)) {
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

        true
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(clubId: String, navController: NavController) {
    var club by remember { mutableStateOf<Club?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isJoined by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("student") }

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

                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
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
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE94560))
                    }
                }
                club == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Club not found", color = Color.White)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }

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
                                        .padding(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFE94560), Color(0xFFF5A623))
                                                )
                                            )
                                            .align(Alignment.CenterHorizontally),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val categoryIcon = when (club!!.category) {
                                            "COMMUNITY_SERVICE" -> "🤝"
                                            "LEADERSHIP" -> "👥"
                                            "CULTURAL" -> "🎭"
                                            "RELIGIOUS" -> "⛪"
                                            "PROFESSIONAL" -> "💼"
                                            "SPORTS" -> "⚽"
                                            "SPECIAL_INTEREST" -> "⭐"
                                            else -> "🏛️"
                                        }
                                        Text(categoryIcon, fontSize = 40.sp)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = club!!.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
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
                                        fontSize = 14.sp,
                                        color = Color(0xFFE94560),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    )

                                    Text(
                                        text = club!!.description,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = Color(0xFFE94560))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${club!!.memberCount} Members", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                if (isJoined) {
                                                    leaveClub(currentUserId, clubId, firestore)
                                                    isJoined = false
                                                } else {
                                                    joinClub(currentUserId, clubId, firestore)
                                                    isJoined = true
                                                }
                                                val updatedClub = firestore.collection("clubs").document(clubId).get().await()
                                                club = updatedClub.toObject(Club::class.java)?.copy(clubId = clubId)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isJoined) Color(0xFFE94560).copy(alpha = 0.2f) else Color(0xFFE94560)
                                        )
                                    ) {
                                        Text(
                                            if (isJoined) "Leave Club" else "Join Club",
                                            color = if (isJoined) Color(0xFFE94560) else Color.White,
                                            fontWeight = FontWeight.Medium
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