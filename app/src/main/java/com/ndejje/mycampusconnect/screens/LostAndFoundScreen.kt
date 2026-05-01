package com.ndejje.mycampusconnect.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.mycampusconnect.models.LostItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostAndFoundScreen(navController: NavController) {
    var lostItems by remember { mutableStateOf<List<LostItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("all") }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun loadItems() {
        scope.launch {
            isLoading = true
            try {
                var query = firestore.collection("lost_items")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

                if (selectedFilter != "all") {
                    query = query.whereEqualTo("status", selectedFilter)
                }

                val snapshot = query.get().await()
                lostItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LostItem::class.java)?.copy(itemId = doc.id)
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedFilter, refreshTrigger) {
        loadItems()
    }

    // Listen for navigation return (refresh when coming back)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lost & Found",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    FloatingActionButton(
                        onClick = { navController.navigate("post_lost_item") },
                        containerColor = Color(0xFFE94560),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Post", tint = Color.White)
                    }
                }

                // Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = when (selectedFilter) {
                        "all" -> 0
                        "lost" -> 1
                        "found" -> 2
                        else -> 0
                    },
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        text = { Text("All", color = Color.White) }
                    )
                    Tab(
                        selected = selectedFilter == "lost",
                        onClick = { selectedFilter = "lost" },
                        text = { Text("Lost", color = Color.White) }
                    )
                    Tab(
                        selected = selectedFilter == "found",
                        onClick = { selectedFilter = "found" },
                        text = { Text("Found", color = Color.White) }
                    )
                }

                // Content
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE94560))
                        }
                    }
                    lostItems.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No items found",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Tap + to post a lost or found item",
                                    fontSize = 12.sp,
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
                            items(lostItems) { item ->
                                LostItemCard(
                                    item = item,
                                    navController = navController
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
fun LostItemCard(
    item: LostItem,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("lost_item_detail/${item.itemId}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.status == "lost")
                        Color(0xFFE94560).copy(alpha = 0.2f)
                    else
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (item.status == "lost") "LOST" else "FOUND",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.status == "lost") Color(0xFFE94560) else Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.location,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Posted by and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Posted by: ${item.userName}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(item.createdAt),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            // Display image if available (Base64)
            item.imageUrl?.let { base64String ->
                if (base64String.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Item image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostLostItemScreen(navController: NavController) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("lost") }
    var isUploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    fun saveToFirestore() {
        scope.launch {
            if (currentUser == null) {
                Toast.makeText(context, "Please log in to post", Toast.LENGTH_SHORT).show()
                return@launch
            }

            isUploading = true

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val userName = userDoc.getString("name") ?: currentUser.email ?: "Anonymous"

                val lostItem = LostItem(
                    itemId = "",
                    userId = currentUser.uid,
                    userName = userName,
                    title = title,
                    description = description,
                    location = location,
                    imageUrl = null, // No image
                    status = status,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("lost_items").add(lostItem).await()
                Toast.makeText(context, "✅ Item posted successfully!", Toast.LENGTH_LONG).show()
                isUploading = false
                navController.navigateUp()

            } catch (e: Exception) {
                isUploading = false
                Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Report Item",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(modifier = Modifier.size(48.dp))
                    }
                }

                // Status Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    selected = status == "lost",
                                    onClick = { status = "lost" },
                                    label = { Text("Lost") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE94560),
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                FilterChip(
                                    selected = status == "found",
                                    onClick = { status = "found" },
                                    label = { Text("Found") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF4CAF50),
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g., Lost Blue Backpack") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFFE94560),
                            cursorColor = Color(0xFFE94560),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Describe the item in detail...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFFE94560),
                            cursorColor = Color(0xFFE94560),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Location
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        placeholder = { Text("Where was it lost/found?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE94560)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFFE94560),
                            cursorColor = Color(0xFFE94560),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Note about photos
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE94560).copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE94560), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ℹ️ Photos are currently disabled due to file size limits. Please provide a detailed description instead.",
                                fontSize = 11.sp,
                                color = Color(0xFFE94560)
                            )
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = { saveToFirestore() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = title.isNotBlank() && description.isNotBlank() && location.isNotBlank() && !isUploading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE94560)
                        )
                    ) {
                        if (isUploading) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Posting...", color = Color.White, fontSize = 14.sp)
                            }
                        } else {
                            Text("Post Item", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostItemDetailScreen(itemId: String, navController: NavController) {
    var item by remember { mutableStateOf<LostItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(itemId) {
        scope.launch {
            try {
                val doc = firestore.collection("lost_items").document(itemId).get().await()
                item = doc.toObject(LostItem::class.java)?.copy(itemId = doc.id)
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
                item == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Item not found", color = Color.White)
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
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (item!!.status == "lost")
                                            Color(0xFFE94560).copy(alpha = 0.2f)
                                        else
                                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            text = if (item!!.status == "lost") "LOST" else "FOUND",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item!!.status == "lost") Color(0xFFE94560) else Color(0xFF4CAF50)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = item!!.title,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Description", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f))
                                    Text(item!!.description, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color(0xFFE94560))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(item!!.location, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color(0xFFE94560))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Posted by: ${item!!.userName}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = Color(0xFFE94560))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(item!!.createdAt),
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.6f)
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