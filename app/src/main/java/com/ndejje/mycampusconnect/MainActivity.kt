package com.ndejje.mycampusconnect

import android.os.Bundle
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ndejje.mycampusconnect.screens.*
import com.ndejje.mycampusconnect.ui.theme.MyCampusConnectTheme
import com.ndejje.mycampusconnect.viewmodels.AuthViewModel

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
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        showSplash = false
    }

    val startDestination = when {
        showSplash -> "splash"
        currentUser != null -> "home"
        else -> "auth"
    }



    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen (optional)
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(if (currentUser != null) "home" else "auth") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Auth Screen (Login/Register)
            composable("auth") {
                AuthScreen(onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                })
            }

            // Main Screens
            composable("home") { MainDashboardScreen(navController) }
            composable("events") { EventsScreen(navController) }
            composable("clubs") { ClubsScreen(navController) }
            composable("lost_and_found") { LostAndFoundScreen(navController) }
            composable("notifications") { NotificationsScreen(navController) }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

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
            composable("seed_data") {
                SeedDataScreen(navController)
            }
        }
    }
}