package com.example.footpronostic.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.footpronostic.data.avatar.AvatarGenerator
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Gère uniquement l'authentification utilisateur (login/register/logout).
 */
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val randomAvatar = AvatarGenerator.random()

                val userData = mapOf(
                    "email" to email,
                    "role" to "USER",
                    "avatar" to randomAvatar
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Firestore error") }
            }
            .addOnFailureListener {
                onError(it.message ?: "Register failed")
            }
    }


    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
