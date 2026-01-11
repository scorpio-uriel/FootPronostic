package com.example.footpronostic.data.repository

import com.example.footpronostic.data.model.Pronostic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Repository pour gérer les pronostics des utilisateurs avec Firestore.
 * Active la persistance pour le mode hors-ligne.
 */
class PronosticRepository {

    private val db = FirebaseFirestore.getInstance()
    private val pronosticsCollection = db.collection("pronostics")

    init {
        // Active la persistance Firestore pour le mode offline
        try {
            db.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        } catch (e: Exception) {
            println("⚠️ Persistance déjà activée: ${e.message}")
        }
    }

    /**
     * Récupère tous les pronostics d'un utilisateur en temps réel.
     */
    fun getUserPronostics(userId: String): Flow<List<Pronostic>> {
        return pronosticsCollection
            .whereEqualTo("userId", userId)
            .orderBy("matchDateTime")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Pronostic>()
            }
    }

    /**
     * Vérifie si l'utilisateur a déjà parié sur un match.
     */
    suspend fun hasUserBetOnMatch(userId: String, matchId: String): Boolean {
        return try {
            val snapshot = pronosticsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("matchId", matchId)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Ajoute un nouveau pronostic.
     */
    suspend fun addPronostic(pronostic: Pronostic): Result<String> {
        return try {
            // Vérifie que le match n'a pas commencé
            if (System.currentTimeMillis() > pronostic.matchDateTime) {
                return Result.failure(Exception("Le match a déjà commencé"))
            }

            val docRef = pronosticsCollection.add(pronostic).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour un pronostic existant (seulement si le match n'a pas commencé).
     */
    suspend fun updatePronostic(pronostic: Pronostic): Result<Unit> {
        return try {
            // Vérifie que le match n'a pas commencé
            if (System.currentTimeMillis() > pronostic.matchDateTime) {
                return Result.failure(Exception("Impossible de modifier : le match a commencé"))
            }

            val updatedPronostic = pronostic.copy(
                updatedAt = System.currentTimeMillis()
            )

            pronosticsCollection.document(pronostic.id).set(updatedPronostic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Supprime un pronostic.
     */
    suspend fun deletePronostic(pronosticId: String): Result<Unit> {
        return try {
            pronosticsCollection.document(pronosticId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère un pronostic par son ID.
     */
    suspend fun getPronosticById(pronosticId: String): Pronostic? {
        return try {
            val document = pronosticsCollection.document(pronosticId).get().await()
            document.toObject(Pronostic::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
