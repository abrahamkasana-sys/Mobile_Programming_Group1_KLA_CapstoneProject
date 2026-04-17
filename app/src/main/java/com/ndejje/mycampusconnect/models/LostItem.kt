package com.ndejje.mycampusconnect.models

data class LostItem(
    val itemId: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String? = null,
    val status: String = "lost", // lost, found, claimed
    val createdAt: Long = System.currentTimeMillis()
)
