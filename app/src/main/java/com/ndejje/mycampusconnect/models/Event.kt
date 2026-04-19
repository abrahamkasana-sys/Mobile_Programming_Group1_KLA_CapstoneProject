package com.ndejje.mycampusconnect.models

data class Event(
    val eventId: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val location: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
