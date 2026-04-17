package com.ndejje.mycampusconnect.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // student, club_leader, admin
    val clubId: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
