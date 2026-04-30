package com.ndejje.mycampusconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.mycampusconnect.models.Club
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDataScreen(navController: NavController) {
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    fun getAllClubsData(): List<Club> {
        return listOf(
            // Community Service Clubs
            Club(
                clubId = "1",
                name = "Rotaract Club of Ndejje University",
                category = "COMMUNITY_SERVICE",
                description = "A prominent community service club focused on professional development and community outreach. Join us to make a difference in the community while developing leadership skills.",
                memberCount = 120,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Leadership & Governance
            Club(
                clubId = "2",
                name = "Student Guild",
                category = "LEADERSHIP",
                description = "The central leadership body, organizing activities and representing student interests. Be the voice of the student body and lead campus initiatives.",
                memberCount = 45,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Cultural Associations
            Club(
                clubId = "3",
                name = "Eastern Region Students' Association",
                category = "CULTURAL",
                description = "Uniting students from Eastern Uganda, celebrating cultural heritage through dance, music, and traditional events.",
                memberCount = 89,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "4",
                name = "Western Uganda Students' Association",
                category = "CULTURAL",
                description = "Promoting cultural unity among students from Western Uganda through cultural exchange programs and events.",
                memberCount = 76,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "5",
                name = "Northern Region Students' Union",
                category = "CULTURAL",
                description = "Celebrating the rich cultural heritage of Northern Uganda through traditional ceremonies and community engagement.",
                memberCount = 64,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Religious Organizations
            Club(
                clubId = "6",
                name = "Ndejje University Christian Union",
                category = "RELIGIOUS",
                description = "A vibrant Christian community focused on spiritual growth, fellowship, and service to others through Bible study and outreach programs.",
                memberCount = 234,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "7",
                name = "Catholic Students Association",
                category = "RELIGIOUS",
                description = "Serving the Catholic community on campus through mass, retreats, and charitable activities.",
                memberCount = 156,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "8",
                name = "Muslim Students Association",
                category = "RELIGIOUS",
                description = "Promoting Islamic values, unity, and understanding among Muslim students on campus.",
                memberCount = 98,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Professional & Academic Associations
            Club(
                clubId = "9",
                name = "Faculty of Computing & IT Association",
                category = "PROFESSIONAL",
                description = "Connecting tech enthusiasts, organizing hackathons, coding workshops, and industry networking events.",
                memberCount = 167,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "10",
                name = "Business Students' Association",
                category = "PROFESSIONAL",
                description = "Professional development through business case competitions, guest speakers, and career networking.",
                memberCount = 189,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "11",
                name = "Health Sciences Association",
                category = "PROFESSIONAL",
                description = "Advancing healthcare education through workshops, medical camps, and research collaborations.",
                memberCount = 145,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "12",
                name = "Education Students' Association",
                category = "PROFESSIONAL",
                description = "Shaping future educators through teaching practice, mentorship, and educational seminars.",
                memberCount = 123,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Sports & Athletics
            Club(
                clubId = "13",
                name = "Ndejje University Football Club",
                category = "SPORTS",
                description = "The university's premier football team competing in university leagues and tournaments.",
                memberCount = 45,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "14",
                name = "Basketball Club",
                category = "SPORTS",
                description = "Fast-paced action and team spirit. Open to all skill levels from beginners to advanced players.",
                memberCount = 56,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "15",
                name = "Athletics Club",
                category = "SPORTS",
                description = "Track and field events including sprints, long distance, jumps, and throws.",
                memberCount = 78,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "16",
                name = "Volleyball Club",
                category = "SPORTS",
                description = "Fun, energetic volleyball training and competitions at inter-university level.",
                memberCount = 42,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),

            // Special Interest Groups
            Club(
                clubId = "17",
                name = "Entrepreneurship Club",
                category = "SPECIAL_INTEREST",
                description = "Turning ideas into businesses. Get mentorship, funding opportunities, and business skills training.",
                memberCount = 94,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "18",
                name = "Environmental Conservation Club",
                category = "SPECIAL_INTEREST",
                description = "Protecting our planet through tree planting, recycling initiatives, and environmental awareness campaigns.",
                memberCount = 112,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "19",
                name = "Debate & Public Speaking Club",
                category = "SPECIAL_INTEREST",
                description = "Hone your communication skills through debates, public speaking contests, and leadership training.",
                memberCount = 67,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            ),
            Club(
                clubId = "20",
                name = "Photography & Media Club",
                category = "SPECIAL_INTEREST",
                description = "Capture campus moments, learn photography skills, and create content for university events.",
                memberCount = 53,
                leaderId = "",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Club Data") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            Text(
                text = "Initialize Club Database",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This will add 20 clubs to your Firestore database.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isUploading = true
                        uploadStatus = "Uploading clubs..."

                        try {
                            val clubs = getAllClubsData()
                            var successCount = 0

                            for (club in clubs) {
                                firestore.collection("clubs")
                                    .document(club.clubId)
                                    .set(club)
                                    .await()
                                successCount++
                            }

                            uploadStatus = "Success! Added $successCount clubs to database."
                        } catch (e: Exception) {
                            uploadStatus = "Error: ${e.message}"
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isUploading) "Uploading..." else "Add Clubs to Database")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uploadStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uploadStatus?.startsWith("Success") == true)
                            Color.Green.copy(alpha = 0.1f)
                        else
                            Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = uploadStatus!!,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uploadStatus?.startsWith("Success") == true) {
                Button(
                    onClick = { navController.navigate("clubs") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Clubs")
                }
            }
        }
    }
}