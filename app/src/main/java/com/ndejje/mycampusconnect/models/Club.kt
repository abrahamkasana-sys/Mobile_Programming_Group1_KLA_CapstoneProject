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

enum class ClubCategory {
    COMMUNITY_SERVICE,    // Rotaract, Rotary, etc.
    LEADERSHIP,           // Student Guild
    CULTURAL,            // Regional/Cultural associations
    RELIGIOUS,           // Christian unions, missions
    PROFESSIONAL,        // Faculty-based associations
    SPORTS,             // Sports teams, athletics
    SPECIAL_INTEREST;   // Hobbies, entrepreneurship, social causes

    fun getDisplayName(): String {
        return when(this) {
            COMMUNITY_SERVICE -> "Community Service"
            LEADERSHIP -> "Leadership & Governance"
            CULTURAL -> "Cultural Associations"
            RELIGIOUS -> "Religious Organizations"
            PROFESSIONAL -> "Professional & Academic"
            SPORTS -> "Sports & Athletics"
            SPECIAL_INTEREST -> "Special Interest"
        }
    }

    fun getIcon(): String {
        return when(this) {
            COMMUNITY_SERVICE -> "🤝"
            LEADERSHIP -> "👥"
            CULTURAL -> "🎭"
            RELIGIOUS -> "⛪"
            PROFESSIONAL -> "💼"
            SPORTS -> "⚽"
            SPECIAL_INTEREST -> "⭐"
        }
    }
}
