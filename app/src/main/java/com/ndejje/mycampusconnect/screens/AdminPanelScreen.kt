package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ndejje.mycampusconnect.models.Announcement
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentUserRole by remember { mutableStateOf("") }
    var isAuthorized by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(Unit) {
        scope.launch {
            if (currentUserId != null) {
                val userDoc = firestore.collection("users").document(currentUserId).get().await()
                currentUserRole = userDoc.getString("role") ?: "student"
                isAuthorized = currentUserRole == "admin"
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
        ) {
            when {
                !isAuthorized -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Unauthorized",
                                modifier = Modifier.size(80.dp),
                                tint = Color(0xFFE94560)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Unauthorized Access",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "You don't have permission to access this page.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigateUp() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
                else -> {
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
                                text = "Admin Panel",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Box(modifier = Modifier.size(48.dp))
                        }

                        // Modern Tab Row
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AdminTabButton(
                                    title = "Stats",
                                    icon = Icons.Default.DateRange,
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                AdminTabButton(
                                    title = "News",
                                    icon = Icons.Default.Info,
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                                AdminTabButton(
                                    title = "Users",
                                    icon = Icons.Default.Person,
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 }
                                )
                                AdminTabButton(
                                    title = "Clubs",
                                    icon = Icons.Default.Person,
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content
                        when (selectedTab) {
                            0 -> AdminDashboard()
                            1 -> AnnouncementsManagement(navController)
                            2 -> UsersManagement()
                            3 -> ClubsManagement()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = if (selected) Color(0xFFE94560) else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            title,
            fontSize = 11.sp,
            color = if (selected) Color(0xFFE94560) else Color.White.copy(alpha = 0.6f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun AdminDashboard() {
    var totalUsers by remember { mutableIntStateOf(0) }
    var totalClubs by remember { mutableIntStateOf(0) }
    var totalEvents by remember { mutableIntStateOf(0) }
    var totalLostItems by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val usersCount = firestore.collection("users").get().await().size()
                val clubsCount = firestore.collection("clubs").get().await().size()
                val eventsCount = firestore.collection("events").get().await().size()
                val lostItemsCount = firestore.collection("lost_items").get().await().size()

                totalUsers = usersCount
                totalClubs = clubsCount
                totalEvents = eventsCount
                totalLostItems = lostItemsCount
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE94560))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                AdminStatsCard(
                    title = "Total Users",
                    value = totalUsers.toString(),
                    icon = Icons.Default.Person,
                    gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                )
            }

            item {
                AdminStatsCard(
                    title = "Total Clubs",
                    value = totalClubs.toString(),
                    icon = Icons.Default.Person,
                    gradient = listOf(Color(0xFFE94560), Color(0xFFF5A623))
                )
            }

            item {
                AdminStatsCard(
                    title = "Total Events",
                    value = totalEvents.toString(),
                    icon = Icons.Default.DateRange,
                    gradient = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                )
            }

            item {
                AdminStatsCard(
                    title = "Lost & Found",
                    value = totalLostItems.toString(),
                    icon = Icons.Default.Search,
                    gradient = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                )
            }
        }
    }
}

@Composable
fun AdminStatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsManagement(navController: NavController) {
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun loadAnnouncements() {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("announcements")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                announcements = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(announcementId = doc.id)
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAnnouncements()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Announcements",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", color = Color.White)
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE94560))
                }
            }
            announcements.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No announcements yet", color = Color.White.copy(alpha = 0.6f))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                        ) {
                            Text("Create First Announcement", color = Color.White)
                        }
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
                        AdminAnnouncementCard(
                            announcement = announcement,
                            onDelete = {
                                scope.launch {
                                    firestore.collection("announcements")
                                        .document(announcement.announcementId)
                                        .delete()
                                        .await()
                                    loadAnnouncements()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAnnouncementDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = {
                showCreateDialog = false
                loadAnnouncements()
            }
        )
    }
}

@Composable
fun AdminAnnouncementCard(
    announcement: Announcement,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE94560).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = announcement.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "By: ${announcement.authorName}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE94560))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.content,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(announcement.createdAt),
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun UsersManagement() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("users")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(userId = doc.id)
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "User Management",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE94560))
                }
            }
            users.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found", color = Color.White.copy(alpha = 0.6f))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        AdminUserCard(user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(user: User) {
    var currentRole by remember { mutableStateOf(user.role) }
    var isUpdating by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun updateRole(newRole: String) {
        scope.launch {
            isUpdating = true
            try {
                firestore.collection("users").document(user.userId)
                    .update("role", newRole)
                    .await()
                currentRole = newRole
                isUpdating = false
                expanded = false
            } catch (_: Exception) {
                isUpdating = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when (currentRole) {
                                "admin" -> Color(0xFFE94560)
                                "club_leader" -> Color(0xFF2196F3)
                                else -> Color(0xFF4CAF50)
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.name.take(2).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (currentRole) {
                            "admin" -> Color(0xFFE94560)
                            "club_leader" -> Color(0xFF2196F3)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = user.email,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Joined: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(user.createdAt)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            if (isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFE94560))
            } else {
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE94560)
                        )
                    ) {
                        Text(currentRole.uppercase(), fontSize = 11.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF16213E))
                    ) {
                        DropdownMenuItem(
                            text = { Text("STUDENT", color = Color.White) },
                            onClick = { updateRole("student") }
                        )
                        DropdownMenuItem(
                            text = { Text("CLUB LEADER", color = Color.White) },
                            onClick = { updateRole("club_leader") }
                        )
                        DropdownMenuItem(
                            text = { Text("ADMIN", color = Color.White) },
                            onClick = { updateRole("admin") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClubsManagement() {
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun loadClubs() {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("clubs")
                    .orderBy("name")
                    .get()
                    .await()
                clubs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Club::class.java)?.copy(clubId = doc.id)
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadClubs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Club Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Club", color = Color.White)
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE94560))
                }
            }
            clubs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No clubs found", color = Color.White.copy(alpha = 0.6f))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                        ) {
                            Text("Create First Club", color = Color.White)
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clubs) { club ->
                        AdminClubCard(
                            club = club,
                            onDelete = {
                                scope.launch {
                                    firestore.collection("clubs").document(club.clubId).delete().await()
                                    loadClubs()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateClubDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = {
                showCreateDialog = false
                loadClubs()
            }
        )
    }
}

@Composable
fun AdminClubCard(club: Club, onDelete: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE94560), Color(0xFFF5A623)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoryIcon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = club.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${club.memberCount} members",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE94560))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAnnouncementDialog(
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    fun createAnnouncement() {
        scope.launch {
            if (currentUser == null) return@launch
            isCreating = true

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val userName = userDoc.getString("name") ?: currentUser.email ?: "Admin"

                val announcement = Announcement(
                    announcementId = "",
                    title = title,
                    content = content,
                    authorId = currentUser.uid,
                    authorName = userName,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("announcements").add(announcement).await()
                isCreating = false
                onCreated()
            } catch (_: Exception) {
                isCreating = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Announcement", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560)
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { createAnnouncement() },
                enabled = title.isNotBlank() && content.isNotBlank() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFE94560))
                } else {
                    Text("Post", color = Color(0xFFE94560))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF16213E)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubDialog(
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    fun createClub() {
        scope.launch {
            isCreating = true
            try {
                val club = Club(
                    clubId = "",
                    name = name,
                    description = description,
                    category = category.uppercase().replace(" ", "_"),
                    leaderId = auth.currentUser?.uid ?: "",
                    memberCount = 1,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("clubs").add(club).await()
                isCreating = false
                onCreated()
            } catch (_: Exception) {
                isCreating = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Club", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Club Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560)
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Academic, Sports, Arts") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE94560),
                        focusedLabelColor = Color(0xFFE94560),
                        cursorColor = Color(0xFFE94560)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { createClub() },
                enabled = name.isNotBlank() && description.isNotBlank() && category.isNotBlank() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFE94560))
                } else {
                    Text("Create", color = Color(0xFFE94560))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF16213E)
    )
}