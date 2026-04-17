package com.ndejje.mycampusconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ndejje.mycampusconnect.ui.screens.*
import com.ndejje.mycampusconnect.ui.theme.MyCampusConnectTheme
import androidx.compose.foundation.layout.fillMaxSize
import com.ndejje.mycampusconnect.screens.SplashScreen
import com.ndejje.mycampusconnect.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCampusConnectTheme {
                CampusConnectApp()
            }
        }
    }
}

@Composable
fun CampusConnectApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "home" else "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash & Auth
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        isLoggedIn = checkIfLoggedIn()
                        navController.navigate(if (isLoggedIn) "home" else "login")
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                        navController.navigate("home")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        isLoggedIn = true
                        navController.navigate("home")
                    }
                )
            }

            // Main Screens
            composable("home") { HomeScreen(navController) }
            composable("events") { EventsScreen(navController) }
            composable("clubs") { ClubsScreen(navController) }
            composable("lost_and_found") { LostAndFoundScreen(navController) }
            composable("notifications") { NotificationsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }

            // Detail Screens
            composable("event_detail/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                EventDetailScreen(eventId ?: "", navController)
            }
            composable("club_detail/{clubId}") { backStackEntry ->
                val clubId = backStackEntry.arguments?.getString("clubId")
                ClubDetailScreen(clubId ?: "", navController)
            }
            composable("lost_item_detail/{itemId}") { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                LostItemDetailScreen(itemId ?: "", navController)
            }

            // Edit/Create Screens
            composable("post_lost_item") { PostLostItemScreen(navController) }
            composable("edit_profile") { EditProfileScreen(navController) }
            composable("admin_panel") { AdminPanelScreen(navController) }
        }
    }
}

fun checkIfLoggedIn(): Boolean {
    return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
}