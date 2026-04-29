package com.ndejje.mycampusconnect.utils

import com.ndejje.mycampusconnect.models.ClubCategory

object CategoryMapper {

    fun stringToEnum(categoryString: String): ClubCategory {
        return when (categoryString.uppercase()) {
            "COMMUNITY_SERVICE" -> ClubCategory.COMMUNITY_SERVICE
            "LEADERSHIP" -> ClubCategory.LEADERSHIP
            "CULTURAL" -> ClubCategory.CULTURAL
            "RELIGIOUS" -> ClubCategory.RELIGIOUS
            "PROFESSIONAL" -> ClubCategory.PROFESSIONAL
            "SPORTS" -> ClubCategory.SPORTS
            "SPECIAL_INTEREST" -> ClubCategory.SPECIAL_INTEREST
            else -> ClubCategory.SPECIAL_INTEREST
        }
    }

    fun enumToString(category: ClubCategory): String = category.name
}