package com.ndejje.mycampusconnect.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.mycampusconnect.models.User
import com.ndejje.mycampusconnect.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.loginUser(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoading.value = false
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.registerUser(email, password, name)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoading.value = false
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepo.logout()
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }
}