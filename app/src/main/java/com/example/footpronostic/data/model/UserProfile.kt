package com.example.footpronostic.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modèle pour le classement et le profil complet avec gestion des rôles.
 */
data class UserProfile(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val points: Int = 0,
    val role: String = "USER",
    val avatar: AvatarConfig = AvatarConfig()
)
