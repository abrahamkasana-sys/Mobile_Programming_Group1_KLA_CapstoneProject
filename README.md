Mobile_Programming_Group1_KLA_CapstoneProject
CampusConnect Youtube link: https://youtu.be/fRFBspAtLuA?si=4nxxHudSjPOrX4of 
A modern campus connectivity app built with Kotlin and Firebase, enabling students to discover clubs, join events, report lost items, and stay connected with university announcements.

Project Identity App Name: CampusConnect 

Team Roster
1.Kasana Abraham 23/2/314/D/036 Lead Developer
2.Muhumuza Philip 25/2/314/DJ/532 UI/UX Designer
3.Gatkuoth Wicjial Nhial 23/2/314/D/070 Database & Firebase Integration
4.Jjuuko Oswald 22/2/314/WJ/268 QA & Testing Engineer
5.Bukenya Brian 23/2/314/w/383 Documentation Specialist

Feature Set Authentication & User Management
Email/Password registration and login
Role-based access (Student, Club Leader, Admin)
Profile management with customizable display names
Clubs System

Browse and search clubs by category (Community Service, Leadership, Cultural, Religious, Professional, Sports, Special Interest)
Join/leave multiple clubs with real-time member count updates
Club detail pages with descriptions and member information
Events Management

View upcoming campus events with dates and locations
Event detail screens with full descriptions
Filter and search functionality
Lost & Found

Report lost or found items with title, description, and location
Image attachment optional (Base64 encoded storage)
Filter by "Lost" or "Found" status
Contact poster functionality
Notifications

Real-time push notifications for announcements
Mark as read/unread functionality
Filter notifications by read status
Admin Panel

User role management (Student/Club Leader/Admin)
Club creation and deletion
Announcement management
Dashboard
Personalized greeting based on time of day

Quick action buttons for common tasks
Upcoming events preview
Popular clubs carousel
Recent announcements feed
Technical Stack Language Kotlin 2.0.0 UI Framework Jetpack Compose 2024.09.00 Backend / Database Firebase Firestore 25.1.1 Authentication Firebase Auth 21.0.1 Image Loading Coil Compose 2.5.0 Navigation Jetpack Compose Navigation 2.7.6 Minimum SDK API 24 (Android 7.0) Target SDK API 36

ViewModels
AuthViewModel: Manages authentication state and logic
ClubsViewModel: Handles club-related business logic

Utilities
CategoryMapper: Maps categories across different features
Additional utility classes for helper functions

Dependencies Used
gradle // Core Android 
implementation("androidx.core:core-ktx:1.18.0") implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0") implementation("androidx.activity:activity-compose:1.13.0")

// Jetpack Compose implementation(platform("androidx.compose:compose-bom:2024.09.00")) implementation("androidx.compose.ui:ui") implementation("androidx.compose.ui:ui-graphics") implementation("androidx.compose.material3:material3")

// Firebase implementation(platform("com.google.firebase:firebase-bom:33.7.0")) implementation("com.google.firebase:firebase-auth-ktx") implementation("com.google.firebase:firebase-firestore-ktx")

// Navigation implementation("androidx.navigation:navigation-compose:2.7.6")

// Image Loading implementation("io.coil-kt:coil-compose:2.5.0")
