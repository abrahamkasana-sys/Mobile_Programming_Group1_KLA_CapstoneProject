package com.ndejje.mycampusconnect.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.mycampusconnect.data.ClubsRepository
import com.ndejje.mycampusconnect.models.Club
import com.ndejje.mycampusconnect.models.ClubCategory
import com.ndejje.mycampusconnect.utils.CategoryMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClubsViewModel : ViewModel() {

    private val _clubs = MutableStateFlow<List<Club>>(emptyList())
    val clubs: StateFlow<List<Club>> = _clubs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchAllClubs() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val clubList = ClubsRepository.getAllClubs()
                _clubs.value = clubList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load clubs"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getClubsByCategory(categoryString: String): List<Club> {
        return _clubs.value.filter { it.category == categoryString }
    }

    fun getClubById(clubId: String): Club? {
        return _clubs.value.find { it.clubId == clubId }
    }

    fun getCategoriesWithClubs(): List<Pair<ClubCategory, List<Club>>> {
        val categoryMap = mutableMapOf<ClubCategory, MutableList<Club>>()

        _clubs.value.forEach { club ->
            val enumCategory = CategoryMapper.stringToEnum(club.category)
            if (!categoryMap.containsKey(enumCategory)) {
                categoryMap[enumCategory] = mutableListOf()
            }
            categoryMap[enumCategory]?.add(club)
        }

        return ClubCategory.values().mapNotNull { category ->
            val clubsInCategory = categoryMap[category] ?: emptyList()
            if (clubsInCategory.isNotEmpty()) {
                category to clubsInCategory
            } else null
        }
    }

    fun joinClub(clubId: String, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = ClubsRepository.joinClub(clubId, userId)
                if (success) {
                    // Refresh clubs to update member count
                    fetchAllClubs()
                }
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}