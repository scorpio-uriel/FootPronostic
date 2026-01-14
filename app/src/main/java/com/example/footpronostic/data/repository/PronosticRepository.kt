package com.example.footpronostic.data.repository

import com.example.footpronostic.data.PointSystem
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.data.model.SportMatch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PronosticRepository {
    private val db = FirebaseFirestore.getInstance()
    private val pronosticsCollection = db.collection("pronostics")
    private val usersCollection = db.collection("users")

    /**
     * Valide automatiquement les pronostics quand un match est terminé.
     * Utilise le moteur de calcul centralisé PointSystem.
     */
    suspend fun validateMatchPronostics(match: SportMatch): Result<Unit> {
        if (match.status != "finished" && match.status != "FINISHED") {
            return Result.failure(Exception("Le match n'est pas encore terminé (Status: ${match.status})"))
        }

        return try {
            val querySnapshot = pronosticsCollection
                .whereEqualTo("matchId", match.id)
                .whereEqualTo("isValidated", false)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.success(Unit) // Rien à valider
            }

            db.runTransaction { transaction ->
                for (doc in querySnapshot.documents) {
                    val pronostic = doc.toObject(Pronostic::class.java) ?: continue

                    val points = PointSystem.calculatePoints(
                        realA = match.scoreA,
                        realB = match.scoreB,
                        predA = pronostic.predictedScoreA,
                        predB = pronostic.predictedScoreB,
                        odds = pronostic.oddsAtBet
                    )

                    transaction.update(
                        doc.reference, mapOf(
                            "isValidated" to true,
                            "pointsGained" to points
                        )
                    )

                    val userRef = usersCollection.document(pronostic.userId)
                    transaction.update(userRef, "points", FieldValue.increment(points.toLong()))
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserPronostics(userId: String): Flow<List<Pronostic>> {
        return pronosticsCollection
            .whereEqualTo("userId", userId)
            .orderBy("matchDateTime")
            .snapshots()
            .map { it.toObjects<Pronostic>() }
    }

    suspend fun addPronostic(pronostic: Pronostic): Result<String> {
        return try {
            val docRef = pronosticsCollection.add(pronostic).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePronostic(pronostic: Pronostic): Result<Unit> {
        return try {
            pronosticsCollection.document(pronostic.id).set(pronostic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePronostic(id: String): Result<Unit> {
        return try {
            pronosticsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserBetOnMatch(userId: String, matchId: String): Boolean {
        val snapshot = pronosticsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("matchId", matchId)
            .get()
            .await()
        return !snapshot.isEmpty
    }
}
