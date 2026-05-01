package com.ndejje.mycampusconnect.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: return@launch

                val user = mapOf(
                    "userId" to userId,
                    "name" to name,
                    "email" to email,
                    "role" to "student",
                    "clubIds" to emptyList<String>(),
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users").document(userId).set(user).await()
                _currentUser.value = auth.currentUser
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun getCurrentUser() = auth.currentUser
}