package com.ndejje.mycampusconnect.models

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val targetRole: String = "all", // all, students, club_leaders, admin
    val createdAt: Long = System.currentTimeMillis()
)
