package com.example.footpronostic.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Gère l'authentification et les informations de profil utilisateur (rôles).
 */
class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // État du rôle de l'utilisateur (admin ou user)
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    init {
        // Au démarrage, si l'utilisateur est déjà connecté, on récupère son rôle
        if (isUserLoggedIn()) {
            fetchUserRole()
        }
    }

    /**
     * Récupère le rôle de l'utilisateur depuis la collection "users" de Firestore.
     */
    fun fetchUserRole() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(userId).get().await()
                _userRole.value = document.getString("role") ?: "user"
            } catch (e: Exception) {
                _userRole.value = "user" // Défaut en cas d'erreur
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fetchUserRole()
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {
                    // À la création, on définit par défaut le rôle sur "user"
                    val userProfile = mapOf("email" to email, "role" to "user")
                    db.collection("users").document(userId).set(userProfile)
                }
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Register failed") }
    }

    fun logout() {
        auth.signOut()
        _userRole.value = null
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}
