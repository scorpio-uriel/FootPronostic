package com.example.footpronostic.data.repository

import com.example.footpronostic.data.model.UserProfile
import com.example.footpronostic.data.model.AvatarConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Récupère le profil d'un utilisateur par son UID.
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                val avatarMap = doc.get("avatar") as? Map<*, *>
                UserProfile(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    points = doc.getLong("points")?.toInt() ?: 0,
                    role = doc.getString("role") ?: "USER",
                    avatar = AvatarConfig(
                        style = avatarMap?.get("style") as? String ?: "avataaars",
                        seed = avatarMap?.get("seed") as? String ?: "default"
                    )
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère les meilleurs utilisateurs pour le classement.
     */
    suspend fun getLeaderboard(limit: Long = 50): List<UserProfile> {
        return try {
            val snapshot = usersCollection
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val avatarMap = doc.get("avatar") as? Map<*, *>
                UserProfile(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    points = doc.getLong("points")?.toInt() ?: 0,
                    role = doc.getString("role") ?: "USER",
                    avatar = AvatarConfig(
                        style = avatarMap?.get("style") as? String ?: "avataaars",
                        seed = avatarMap?.get("seed") as? String ?: "default"
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Met à jour manuellement les points (utilisé par l'admin).
     */
    suspend fun updatePoints(uid: String, increment: Int): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update("points", com.google.firebase.firestore.FieldValue.increment(increment.toLong()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
