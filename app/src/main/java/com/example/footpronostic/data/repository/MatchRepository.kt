package com.example.footpronostic.data.repository

import com.example.footpronostic.data.model.SportMatch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Gère les opérations de données pour les matchs avec Firebase Firestore.
 * Ce repository active la persistance des données pour une utilisation hors-ligne.
 */
class MatchRepository {

    // Obtient une instance de Firestore et la référence à la collection "matches".
    private val db = FirebaseFirestore.getInstance()
    private val matchesCollection = db.collection("matches")

    init {
        // Active la persistance des données pour permettre le mode hors-ligne.
        // Les données seront stockées localement et synchronisées avec Firestore
        // lorsque l'appareil sera de nouveau en ligne.
        db.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    /**
     * Récupère la liste des matchs en temps réel depuis Firestore.
     * @return Un Flow qui émet la liste des matchs à chaque mise à jour.
     */
    fun getMatches(): Flow<List<SportMatch>> {
        // La méthode snapshots() écoute les changements en temps réel sur la collection.
        return matchesCollection.snapshots().map { snapshot ->
            // Convertit les documents Firestore en une liste d'objets SportMatch.
            snapshot.toObjects<SportMatch>()
        }
    }

    /**
     * Ajoute un nouveau match dans Firestore.
     * @param match Le match à ajouter.
     */
    suspend fun addMatch(match: SportMatch) {
        // Ajoute le document à la collection. Firestore génère automatiquement un ID.
        matchesCollection.add(match).await()
    }

    /**
     * Met à jour un match existant dans Firestore.
     * @param match Le match contenant les nouvelles données.
     */
    suspend fun updateMatch(match: SportMatch) {
        // Met à jour le document correspondant à l'ID du match.
        matchesCollection.document(match.id).set(match).await()
    }

    /**
     * Supprime un match de Firestore.
     * @param matchId L'identifiant du match à supprimer.
     */
    suspend fun deleteMatch(matchId: String) {
        // Supprime le document en utilisant son ID.
        matchesCollection.document(matchId).delete().await()
    }
}
