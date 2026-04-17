package com.ndejje.mycampusconnect.models

data class Club(
    val clubId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val leaderId: String = "",
    val memberCount: Int = 0,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
