package com.ndejje.mycampusconnect.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ndejje.mycampusconnect.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.ndejje.mycampusconnect.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf("student") }
    var userClub by remember { mutableStateOf<String?>(null) }
    var myEventsCount by remember { mutableStateOf(0) }
    var myLostItemsCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                if (currentUser != null) {
                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                    user = userDoc.toObject(User::class.java)
                    userRole = user?.role ?: "student"
                    userClub = user?.clubId

                    // Count user's events
                    val eventsSnapshot = firestore.collection("events")
                        .whereEqualTo("clubId", userClub ?: "")
                        .get()
                        .await()
                    myEventsCount = eventsSnapshot.size()

                    // Count user's lost items
                    val lostItemsSnapshot = firestore.collection("lost_items")
                        .whereEqualTo("userId", currentUser.uid)
                        .get()
                        .await()
                    myLostItemsCount = lostItemsSnapshot.size()
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("edit_profile") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login")
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
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
            user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("User not found")
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
                    // Profile Header
                    item {
                        ProfileHeader(user = user!!)
                    }

                    // Stats Cards
                    item {
                        StatsRow(
                            eventsCount = myEventsCount,
                            lostItemsCount = myLostItemsCount
                        )
                    }

                    // User Info Card
                    item {
                        UserInfoCard(user = user!!, userClub = userClub)
                    }

                    // Role-Based Actions
                    if (userRole == "admin") {
                        item {
                            AdminActions(navController)
                        }
                    }

                    if (userRole == "club_leader" && userClub != null) {
                        item {
                            ClubLeaderActions(navController)
                        }
                    }

                    // My Posts Section
                    item {
                        Text(
                            text = "My Content",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        MyPostsCard(
                            onMyEventsClick = { /* Navigate to my events */ },
                            onMyLostItemsClick = { /* Navigate to my lost items */ }
                        )
                    }

                    // Settings Section
                    item {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        SettingsCard(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (user.profileImageUrl != null) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // User Email
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Role Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (user.role) {
                    "admin" -> MaterialTheme.colorScheme.errorContainer
                    "club_leader" -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = user.role.uppercase().replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (user.role) {
                        "admin" -> MaterialTheme.colorScheme.error
                        "club_leader" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

@Composable
fun StatsRow(eventsCount: Int, lostItemsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "Events",
            value = eventsCount.toString(),
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Event
        )
        StatsCard(
            title = "Lost Items",
            value = lostItemsCount.toString(),
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Search
        )
    }
}

@Composable
fun StatsCard(title: String, value: String, modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserInfoCard(user: User, userClub: String?) {
    var clubName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(userClub) {
        if (userClub != null) {
            scope.launch {
                try {
                    val clubDoc = firestore.collection("clubs").document(userClub!!).get().await()
                    clubName = clubDoc.getString("name")
                } catch (e: Exception) {
                    // Club not found
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(
                label = "Full Name",
                value = user.name
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Email Address",
                value = user.email
            )

            if (clubName != null) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(
                    label = "Club Membership",
                    value = clubName ?: "None"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Member Since",
                value = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
                    .format(user.createdAt)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AdminActions(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Admin Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("admin_panel") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Admin Dashboard")
            }
        }
    }
}

@Composable
fun ClubLeaderActions(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Club Leader Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Navigate to create event */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Event")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { /* Navigate to manage club */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Club")
            }
        }
    }
}

@Composable
fun MyPostsCard(
    onMyEventsClick: () -> Unit,
    onMyLostItemsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Event,
                title = "My Events",
                onClick = onMyEventsClick
            )

            Divider()

            SettingsItem(
                icon = Icons.Default.Search,
                title = "My Lost & Found Posts",
                onClick = onMyLostItemsClick
            )
        }
    }
}

@Composable
fun SettingsCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                onClick = { navController.navigate("notifications") }
            )

            Divider()

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                onClick = { /* Show privacy policy */ }
            )

            Divider()

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                onClick = { /* Show about */ }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Load current user data
    LaunchedEffect(Unit) {
        scope.launch {
            if (currentUser != null) {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                name = userDoc.getString("name") ?: ""
                profileImageUrl = userDoc.getString("profileImageUrl")
            }
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    fun updateProfile() {
        scope.launch {
            if (currentUser == null) return@launch
            isLoading = true

            try {
                var newImageUrl = profileImageUrl

                // Upload new image if selected
                selectedImageUri?.let { uri ->
                    val storageRef = storage.reference
                    val imageRef = storageRef.child("profile_images/${currentUser.uid}.jpg")

                    val inputStream = navController.context.contentResolver.openInputStream(uri)
                    inputStream?.let {
                        imageRef.putStream(it).await()
                        newImageUrl = imageRef.downloadUrl.await().toString()
                    }
                }

                // Update Firestore
                val updates = mapOf(
                    "name" to name,
                    "profileImageUrl" to (newImageUrl ?: "")
                )
                firestore.collection("users").document(currentUser.uid).update(updates).await()

                // Update Auth display name
                val userProfile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                currentUser.updateProfile(userProfile).await()

                isLoading = false
                navController.navigateUp()
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    TextButton(
                        onClick = { updateProfile() },
                        enabled = !isLoading && name.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    selectedImageUri != null -> {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Selected profile picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    profileImageUrl != null && profileImageUrl!!.isNotEmpty() -> {
                                        AsyncImage(
                                            model = profileImageUrl,
                                            contentDescription = "Profile picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = name.take(2).uppercase(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to change photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true
                )
            }
        }
    }
}

// Add missing imports
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable